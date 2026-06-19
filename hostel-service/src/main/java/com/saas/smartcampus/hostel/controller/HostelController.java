package com.saas.smartcampus.hostel.controller;

import com.saas.smartcampus.hostel.dto.AllocationRequest;
import com.saas.smartcampus.hostel.entity.Allocation;
import com.saas.smartcampus.hostel.entity.Room;
import com.saas.smartcampus.hostel.service.HostelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostel")
public class HostelController {

    private final HostelService service;

    public HostelController(HostelService service) {
        this.service = service;
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> addRoom(@RequestBody Room room) {
        Room created = service.addRoom(room);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(service.getAllRooms());
    }

    @PostMapping("/allocate")
    public ResponseEntity<Allocation> allocateRoom(@RequestBody AllocationRequest request) {
        Allocation allocation = service.allocateRoom(request.getRoomId(), request.getStudentId());
        return new ResponseEntity<>(allocation, HttpStatus.CREATED);
    }

    @PostMapping("/deallocate/{allocationId}")
    public ResponseEntity<Allocation> deallocateRoom(@PathVariable Long allocationId) {
        Allocation deallocated = service.deallocateRoom(allocationId);
        return ResponseEntity.ok(deallocated);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Allocation>> getAllocationsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getAllocationsByStudent(studentId));
    }
}
