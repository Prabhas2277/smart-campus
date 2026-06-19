package com.saas.smartcampus.attendance.entity;

import com.saas.smartcampus.shared.entity.AbstractTenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_records")
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public class AttendanceRecord extends AbstractTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, LATE, EXCUSED

    // Constructors
    public AttendanceRecord() {}

    public AttendanceRecord(Long id, Long studentId, LocalDate date, String status) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Builder
    public static AttendanceRecordBuilder builder() {
        return new AttendanceRecordBuilder();
    }

    public static class AttendanceRecordBuilder {
        private Long id;
        private Long studentId;
        private LocalDate date;
        private String status;
        private String tenantId;

        public AttendanceRecordBuilder id(Long id) { this.id = id; return this; }
        public AttendanceRecordBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public AttendanceRecordBuilder date(LocalDate date) { this.date = date; return this; }
        public AttendanceRecordBuilder status(String status) { this.status = status; return this; }
        public AttendanceRecordBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public AttendanceRecord build() {
            AttendanceRecord record = new AttendanceRecord(id, studentId, date, status);
            if (tenantId != null) {
                record.setTenantId(tenantId);
            }
            return record;
        }
    }
}
