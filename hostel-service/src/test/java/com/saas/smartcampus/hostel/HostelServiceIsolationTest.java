package com.saas.smartcampus.hostel;

import com.saas.smartcampus.hostel.entity.Allocation;
import com.saas.smartcampus.hostel.entity.Room;
import com.saas.smartcampus.hostel.repository.AllocationRepository;
import com.saas.smartcampus.hostel.repository.RoomRepository;
import com.saas.smartcampus.hostel.service.HostelService;
import com.saas.smartcampus.shared.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HostelServiceIsolationTest {

    @Autowired
    private HostelService service;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @AfterEach
    public void cleanup() {
        TenantContext.setCurrentTenant("tenant-a");
        allocationRepository.deleteAll();
        roomRepository.deleteAll();
        TenantContext.setCurrentTenant("tenant-b");
        allocationRepository.deleteAll();
        roomRepository.deleteAll();
        TenantContext.clear();
    }

    @Test
    public void testTenantIsolationOnHostel() {
        // 1. Add Room on Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        Room roomA = Room.builder()
                .roomNumber("A-101")
                .hostelName("Boys Hostel")
                .capacity(2)
                .occupancy(0)
                .build();
        service.addRoom(roomA);

        // 2. Add Room on Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        Room roomB = Room.builder()
                .roomNumber("B-201")
                .hostelName("Girls Hostel")
                .capacity(3)
                .occupancy(0)
                .build();
        service.addRoom(roomB);

        // 3. Query as Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        List<Room> listA = service.getAllRooms();
        assertEquals(1, listA.size());
        assertEquals("A-101", listA.get(0).getRoomNumber());

        // 4. Query as Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        List<Room> listB = service.getAllRooms();
        assertEquals(1, listB.size());
        assertEquals("B-201", listB.get(0).getRoomNumber());
    }

    @Test
    public void testHostelBusinessRules() {
        TenantContext.setCurrentTenant("tenant-a");

        // 1. Create a Room with capacity 1
        Room room = Room.builder()
                .roomNumber("A-102")
                .hostelName("Boys Hostel")
                .capacity(1)
                .occupancy(0)
                .build();
        Room savedRoom = service.addRoom(room);

        // 2. Allocate student 888L and check occupancy becomes 1
        Allocation allocation1 = service.allocateRoom(savedRoom.getId(), 888L);
        assertNotNull(allocation1);
        assertEquals("ACTIVE", allocation1.getStatus());

        Room roomAfterAlloc = roomRepository.findById(savedRoom.getId()).orElseThrow();
        assertEquals(1, roomAfterAlloc.getOccupancy()); // Occupancy is now 1 (Full)

        // 3. Try to allocate another student 999L -> should throw exception
        assertThrows(IllegalStateException.class, () -> {
            service.allocateRoom(savedRoom.getId(), 999L);
        });

        // 4. Deallocate room for first student -> occupancy should return to 0
        Allocation deallocated = service.deallocateRoom(allocation1.getId());
        assertEquals("TERMINATED", deallocated.getStatus());

        Room roomAfterDealloc = roomRepository.findById(savedRoom.getId()).orElseThrow();
        assertEquals(0, roomAfterDealloc.getOccupancy()); // Occupancy is back to 0
    }
}
