package com.saas.smartcampus.hostel.repository;

import com.saas.smartcampus.hostel.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByStudentId(Long studentId);
}
