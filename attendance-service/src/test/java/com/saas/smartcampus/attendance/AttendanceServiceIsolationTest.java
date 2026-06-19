package com.saas.smartcampus.attendance;

import com.saas.smartcampus.attendance.entity.AttendanceRecord;
import com.saas.smartcampus.attendance.repository.AttendanceRecordRepository;
import com.saas.smartcampus.attendance.service.AttendanceService;
import com.saas.smartcampus.shared.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AttendanceServiceIsolationTest {

    @Autowired
    private AttendanceService service;

    @Autowired
    private AttendanceRecordRepository repository;

    @AfterEach
    public void cleanup() {
        TenantContext.setCurrentTenant("tenant-a");
        repository.deleteAll();
        TenantContext.setCurrentTenant("tenant-b");
        repository.deleteAll();
        TenantContext.clear();
    }

    @Test
    public void testTenantIsolationOnAttendance() {
        // 1. Mark attendance for Student 101 on Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        AttendanceRecord recordA = AttendanceRecord.builder()
                .studentId(101L)
                .date(LocalDate.now())
                .status("PRESENT")
                .build();
        service.markAttendance(recordA);

        // 2. Mark attendance for Student 101 on Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        AttendanceRecord recordB = AttendanceRecord.builder()
                .studentId(101L)
                .date(LocalDate.now())
                .status("ABSENT")
                .build();
        service.markAttendance(recordB);

        // 3. Query as Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        List<AttendanceRecord> listA = service.getAttendanceByStudent(101L);
        assertEquals(1, listA.size());
        assertEquals("PRESENT", listA.get(0).getStatus());
        assertEquals("tenant-a", listA.get(0).getTenantId());

        // 4. Query as Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        List<AttendanceRecord> listB = service.getAttendanceByStudent(101L);
        assertEquals(1, listB.size());
        assertEquals("ABSENT", listB.get(0).getStatus());
        assertEquals("tenant-b", listB.get(0).getTenantId());
    }
}
