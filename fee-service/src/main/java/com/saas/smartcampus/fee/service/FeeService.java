package com.saas.smartcampus.fee.service;

import com.saas.smartcampus.fee.client.AuthClient;
import com.saas.smartcampus.fee.dto.UserDto;
import com.saas.smartcampus.fee.entity.FeeStructure;
import com.saas.smartcampus.fee.entity.Payment;
import com.saas.smartcampus.fee.repository.FeeStructureRepository;
import com.saas.smartcampus.fee.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeeService {

    private static final Logger log = LoggerFactory.getLogger(FeeService.class);

    private final FeeStructureRepository feeStructureRepository;
    private final PaymentRepository paymentRepository;
    private final AuthClient authClient;

    public FeeService(FeeStructureRepository feeStructureRepository, PaymentRepository paymentRepository, AuthClient authClient) {
        this.feeStructureRepository = feeStructureRepository;
        this.paymentRepository = paymentRepository;
        this.authClient = authClient;
    }

    @Transactional
    public FeeStructure addFeeStructure(FeeStructure structure) {
        log.info("Adding fee structure: name={}, amount={}", structure.getName(), structure.getAmount());
        return feeStructureRepository.save(structure);
    }

    @Transactional(readOnly = true)
    public List<FeeStructure> getAllFeeStructures() {
        return feeStructureRepository.findAll();
    }

    @Transactional
    public Payment recordPayment(Long feeStructureId, Long studentId, BigDecimal amountPaid) {
        log.info("Recording payment of ${} for student ID {} against fee structure ID {}", amountPaid, studentId, feeStructureId);
        
        // Call auth-service to check if student exists
        try {
            UserDto student = authClient.getUserById(studentId);
            if (student == null || !"STUDENT".equalsIgnoreCase(student.getRole())) {
                throw new IllegalArgumentException("User with ID " + studentId + " is not registered as a STUDENT!");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist in this tenant! Error: " + e.getMessage());
        }

        FeeStructure structure = feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new IllegalArgumentException("Fee structure with ID " + feeStructureId + " not found!"));

        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero!");
        }

        String receiptNumber = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .feeStructureId(feeStructureId)
                .studentId(studentId)
                .amountPaid(amountPaid)
                .paymentDate(LocalDateTime.now())
                .receiptNumber(receiptNumber)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getStudentBalance(Long studentId) {
        // Total fees in the tenant
        BigDecimal totalFees = feeStructureRepository.findAll().stream()
                .map(FeeStructure::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total paid by student
        BigDecimal totalPaid = paymentRepository.findByStudentId(studentId).stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalFees.subtract(totalPaid);
        log.info("Calculated balance for student ID {}: Total Fees = ${}, Total Paid = ${}, Balance = ${}",
                studentId, totalFees, totalPaid, balance);
        return balance;
    }
}
