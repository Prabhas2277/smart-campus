package com.saas.smartcampus.hostel.entity;

import com.saas.smartcampus.shared.entity.AbstractTenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

@Entity
@Table(name = "allocations")
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public class Allocation extends AbstractTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "allocation_date", nullable = false)
    private LocalDate allocationDate;

    @Column(nullable = false)
    private String status; // ACTIVE, TERMINATED

    // Constructors
    public Allocation() {}

    public Allocation(Long id, Long roomId, Long studentId, LocalDate allocationDate, String status) {
        this.id = id;
        this.roomId = roomId;
        this.studentId = studentId;
        this.allocationDate = allocationDate;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public LocalDate getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDate allocationDate) { this.allocationDate = allocationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Builder
    public static AllocationBuilder builder() {
        return new AllocationBuilder();
    }

    public static class AllocationBuilder {
        private Long id;
        private Long roomId;
        private Long studentId;
        private LocalDate allocationDate;
        private String status;
        private String tenantId;

        public AllocationBuilder id(Long id) { this.id = id; return this; }
        public AllocationBuilder roomId(Long roomId) { this.roomId = roomId; return this; }
        public AllocationBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public AllocationBuilder allocationDate(LocalDate allocationDate) { this.allocationDate = allocationDate; return this; }
        public AllocationBuilder status(String status) { this.status = status; return this; }
        public AllocationBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public Allocation build() {
            Allocation allocation = new Allocation(id, roomId, studentId, allocationDate, status);
            if (tenantId != null) {
                allocation.setTenantId(tenantId);
            }
            return allocation;
        }
    }
}
