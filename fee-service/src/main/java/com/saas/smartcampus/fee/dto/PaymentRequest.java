package com.saas.smartcampus.fee.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Long feeStructureId;
    private Long studentId;
    private BigDecimal amountPaid;

    public PaymentRequest() {}

    public PaymentRequest(Long feeStructureId, Long studentId, BigDecimal amountPaid) {
        this.feeStructureId = feeStructureId;
        this.studentId = studentId;
        this.amountPaid = amountPaid;
    }

    public Long getFeeStructureId() { return feeStructureId; }
    public void setFeeStructureId(Long feeStructureId) { this.feeStructureId = feeStructureId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
}
