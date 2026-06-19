package com.saas.smartcampus.hostel.service;

import com.saas.smartcampus.hostel.entity.Allocation;
import com.saas.smartcampus.hostel.entity.Room;
import com.saas.smartcampus.hostel.repository.AllocationRepository;
import com.saas.smartcampus.hostel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HostelService {

    private static final Logger log = LoggerFactory.getLogger(HostelService.class);

    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;

    public HostelService(RoomRepository roomRepository, AllocationRepository allocationRepository) {
        this.roomRepository = roomRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public Room addRoom(Room room) {
        log.info("Adding room to inventory: roomNumber={}, hostelName={}", room.getRoomNumber(), room.getHostelName());
        if (room.getOccupancy() < 0) {
            room.setOccupancy(0);
        }
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional
    public Allocation allocateRoom(Long roomId, Long studentId) {
        log.info("Attempting to allocate student ID {} to room ID {}", studentId, roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found!"));

        if (room.getOccupancy() >= room.getCapacity()) {
            throw new IllegalStateException("Room '" + room.getRoomNumber() + "' is fully occupied! (Capacity: " + room.getCapacity() + ")");
        }

        // Increment occupancy
        room.setOccupancy(room.getOccupancy() + 1);
        roomRepository.save(room);

        Allocation allocation = Allocation.builder()
                .roomId(roomId)
                .studentId(studentId)
                .allocationDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        return allocationRepository.save(allocation);
    }

    @Transactional
    public Allocation deallocateRoom(Long allocationId) {
        log.info("Processing deallocation for allocation ID {}", allocationId);
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Allocation with ID " + allocationId + " not found!"));

        if ("TERMINATED".equals(allocation.getStatus())) {
            throw new IllegalStateException("Hostel allocation has already been terminated!");
        }

        allocation.setStatus("TERMINATED");

        Room room = roomRepository.findById(allocation.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room associated with allocation record not found!"));

        // Decrement occupancy
        if (room.getOccupancy() > 0) {
            room.setOccupancy(room.getOccupancy() - 1);
        }
        roomRepository.save(room);

        return allocationRepository.save(allocation);
    }

    @Transactional(readOnly = true)
    public List<Allocation> getAllocationsByStudent(Long studentId) {
        return allocationRepository.findByStudentId(studentId);
    }
}
