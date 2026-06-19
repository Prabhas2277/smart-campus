package com.saas.smartcampus.hostel.dto;

public class AllocationRequest {
    private Long roomId;
    private Long studentId;

    public AllocationRequest() {}

    public AllocationRequest(Long roomId, Long studentId) {
        this.roomId = roomId;
        this.studentId = studentId;
    }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
