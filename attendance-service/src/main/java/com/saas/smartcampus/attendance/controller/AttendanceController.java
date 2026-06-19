package com.saas.smartcampus.attendance.controller;

import com.saas.smartcampus.attendance.entity.AttendanceRecord;
import com.saas.smartcampus.attendance.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AttendanceRecord> markAttendance(@RequestBody AttendanceRecord record) {
        AttendanceRecord created = service.markAttendance(record);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceRecord>> getAttendanceByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getAttendanceByStudent(studentId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceRecord>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getAttendanceByDate(date));
    }
}
