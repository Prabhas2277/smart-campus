package com.saas.smartcampus.attendance.service;

import com.saas.smartcampus.attendance.entity.AttendanceRecord;
import com.saas.smartcampus.attendance.repository.AttendanceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final AttendanceRecordRepository repository;

    public AttendanceService(AttendanceRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AttendanceRecord markAttendance(AttendanceRecord record) {
        log.info("Marking attendance for student: {}, date: {}, status: {}", 
                record.getStudentId(), record.getDate(), record.getStatus());
        return repository.save(record);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceByStudent(Long studentId) {
        log.info("Fetching attendance records for student: {}", studentId);
        return repository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceByDate(LocalDate date) {
        log.info("Fetching attendance records for date: {}", date);
        return repository.findByDate(date);
    }
}
