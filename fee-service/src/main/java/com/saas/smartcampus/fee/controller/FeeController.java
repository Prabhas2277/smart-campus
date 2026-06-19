package com.saas.smartcampus.fee.controller;

import com.saas.smartcampus.fee.dto.PaymentRequest;
import com.saas.smartcampus.fee.entity.FeeStructure;
import com.saas.smartcampus.fee.entity.Payment;
import com.saas.smartcampus.fee.service.FeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeService service;

    public FeeController(FeeService service) {
        this.service = service;
    }

    @PostMapping("/structures")
    public ResponseEntity<FeeStructure> addFeeStructure(@RequestBody FeeStructure structure) {
        FeeStructure created = service.addFeeStructure(structure);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/structures")
    public ResponseEntity<List<FeeStructure>> getAllFeeStructures() {
        return ResponseEntity.ok(service.getAllFeeStructures());
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> recordPayment(@RequestBody PaymentRequest request) {
        Payment payment = service.recordPayment(request.getFeeStructureId(), request.getStudentId(), request.getAmountPaid());
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Payment>> getPaymentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getPaymentsByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/balance")
    public ResponseEntity<BigDecimal> getStudentBalance(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentBalance(studentId));
    }
}
