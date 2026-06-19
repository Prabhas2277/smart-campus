package com.saas.smartcampus.library.dto;

public class IssueRequest {
    private Long bookId;
    private Long studentId;

    public IssueRequest() {}

    public IssueRequest(Long bookId, Long studentId) {
        this.bookId = bookId;
        this.studentId = studentId;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
