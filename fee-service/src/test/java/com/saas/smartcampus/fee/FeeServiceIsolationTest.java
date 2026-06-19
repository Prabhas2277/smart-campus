package com.saas.smartcampus.fee;

import com.saas.smartcampus.fee.entity.FeeStructure;
import com.saas.smartcampus.fee.entity.Payment;
import com.saas.smartcampus.fee.repository.FeeStructureRepository;
import com.saas.smartcampus.fee.repository.PaymentRepository;
import com.saas.smartcampus.fee.service.FeeService;
import com.saas.smartcampus.shared.context.TenantContext;
import com.saas.smartcampus.fee.client.AuthClient;
import com.saas.smartcampus.fee.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FeeServiceIsolationTest {

    @Autowired
    private FeeService service;

    @Autowired
    private FeeStructureRepository feeStructureRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private AuthClient authClient;

    @BeforeEach
    public void setup() {
        UserDto studentDto = new UserDto(777L, "John Student", "john@campus.com", "STUDENT");
        Mockito.when(authClient.getUserById(777L)).thenReturn(studentDto);
    }

    @AfterEach
    public void cleanup() {
        TenantContext.setCurrentTenant("tenant-a");
        paymentRepository.deleteAll();
        feeStructureRepository.deleteAll();
        TenantContext.setCurrentTenant("tenant-b");
        paymentRepository.deleteAll();
        feeStructureRepository.deleteAll();
        TenantContext.clear();
    }

    @Test
    public void testTenantIsolationOnFees() {
        // 1. Add Fee Structure on Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        FeeStructure structureA = FeeStructure.builder()
                .name("Tuition Fee FY26")
                .amount(new BigDecimal("5000.00"))
                .dueDate(LocalDate.now().plusMonths(3))
                .build();
        service.addFeeStructure(structureA);

        // 2. Add Fee Structure on Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        FeeStructure structureB = FeeStructure.builder()
                .name("Library Fee FY26")
                .amount(new BigDecimal("300.00"))
                .dueDate(LocalDate.now().plusMonths(2))
                .build();
        service.addFeeStructure(structureB);

        // 3. Query as Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        List<FeeStructure> listA = service.getAllFeeStructures();
        assertEquals(1, listA.size());
        assertEquals("Tuition Fee FY26", listA.get(0).getName());

        // 4. Query as Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        List<FeeStructure> listB = service.getAllFeeStructures();
        assertEquals(1, listB.size());
        assertEquals("Library Fee FY26", listB.get(0).getName());
    }

    @Test
    public void testFeePaymentAndBalance() {
        TenantContext.setCurrentTenant("tenant-a");

        // 1. Add fee structure (Total Fee = $1000)
        FeeStructure structure = FeeStructure.builder()
                .name("Term 1 Tuition")
                .amount(new BigDecimal("1000.00"))
                .dueDate(LocalDate.now().plusDays(30))
                .build();
        FeeStructure savedStructure = service.addFeeStructure(structure);

        // 2. Initial balance check -> balance should be $1000
        BigDecimal balance0 = service.getStudentBalance(777L);
        assertEquals(new BigDecimal("1000.00"), balance0);

        // 3. Record payment of $400
        Payment payment = service.recordPayment(savedStructure.getId(), 777L, new BigDecimal("400.00"));
        assertNotNull(payment);
        assertTrue(payment.getReceiptNumber().startsWith("REC-"));
        assertEquals(new BigDecimal("400.00"), payment.getAmountPaid());

        // 4. Check balance after payment -> balance should be $600
        BigDecimal balance1 = service.getStudentBalance(777L);
        assertEquals(new BigDecimal("600.00"), balance1);
    }
}
