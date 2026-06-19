package com.saas.smartcampus.hostel.entity;

import com.saas.smartcampus.shared.entity.AbstractTenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "rooms")
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public class Room extends AbstractTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "hostel_name", nullable = false)
    private String hostelName;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int occupancy;

    // Constructors
    public Room() {}

    public Room(Long id, String roomNumber, String hostelName, int capacity, int occupancy) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.hostelName = hostelName;
        this.capacity = capacity;
        this.occupancy = occupancy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }

    // Builder
    public static RoomBuilder builder() {
        return new RoomBuilder();
    }

    public static class RoomBuilder {
        private Long id;
        private String roomNumber;
        private String hostelName;
        private int capacity;
        private int occupancy;
        private String tenantId;

        public RoomBuilder id(Long id) { this.id = id; return this; }
        public RoomBuilder roomNumber(String roomNumber) { this.roomNumber = roomNumber; return this; }
        public RoomBuilder hostelName(String hostelName) { this.hostelName = hostelName; return this; }
        public RoomBuilder capacity(int capacity) { this.capacity = capacity; return this; }
        public RoomBuilder occupancy(int occupancy) { this.occupancy = occupancy; return this; }
        public RoomBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public Room build() {
            Room room = new Room(id, roomNumber, hostelName, capacity, occupancy);
            if (tenantId != null) {
                room.setTenantId(tenantId);
            }
            return room;
        }
    }
}
