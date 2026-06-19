package com.saas.smartcampus.fee.entity;

import com.saas.smartcampus.shared.entity.AbstractTenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public class Payment extends AbstractTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "fee_structure_id", nullable = false)
    private Long feeStructureId;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    // Constructors
    public Payment() {}

    public Payment(Long id, Long studentId, Long feeStructureId, BigDecimal amountPaid, LocalDateTime paymentDate, String receiptNumber) {
        this.id = id;
        this.studentId = studentId;
        this.feeStructureId = feeStructureId;
        this.amountPaid = amountPaid != null ? amountPaid : BigDecimal.ZERO;
        this.paymentDate = paymentDate;
        this.receiptNumber = receiptNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getFeeStructureId() { return feeStructureId; }
    public void setFeeStructureId(Long feeStructureId) { this.feeStructureId = feeStructureId; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    // Builder
    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private Long studentId;
        private Long feeStructureId;
        private BigDecimal amountPaid;
        private LocalDateTime paymentDate;
        private String receiptNumber;
        private String tenantId;

        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public PaymentBuilder feeStructureId(Long feeStructureId) { this.feeStructureId = feeStructureId; return this; }
        public PaymentBuilder amountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; return this; }
        public PaymentBuilder paymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; return this; }
        public PaymentBuilder receiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; return this; }
        public PaymentBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public Payment build() {
            Payment payment = new Payment(id, studentId, feeStructureId, amountPaid, paymentDate, receiptNumber);
            if (tenantId != null) {
                payment.setTenantId(tenantId);
            }
            return payment;
        }
    }
}
