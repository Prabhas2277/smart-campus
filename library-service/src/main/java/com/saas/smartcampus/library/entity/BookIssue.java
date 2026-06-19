package com.saas.smartcampus.library.entity;

import com.saas.smartcampus.shared.entity.AbstractTenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "book_issues")
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public class BookIssue extends AbstractTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "fine_amount", nullable = false)
    private BigDecimal fineAmount;

    // Constructors
    public BookIssue() {}

    public BookIssue(Long id, Long bookId, Long studentId, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, BigDecimal fineAmount) {
        this.id = id;
        this.bookId = bookId;
        this.studentId = studentId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount != null ? fineAmount : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }

    // Builder
    public static BookIssueBuilder builder() {
        return new BookIssueBuilder();
    }

    public static class BookIssueBuilder {
        private Long id;
        private Long bookId;
        private Long studentId;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private LocalDate returnDate;
        private BigDecimal fineAmount;
        private String tenantId;

        public BookIssueBuilder id(Long id) { this.id = id; return this; }
        public BookIssueBuilder bookId(Long bookId) { this.bookId = bookId; return this; }
        public BookIssueBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public BookIssueBuilder issueDate(LocalDate issueDate) { this.issueDate = issueDate; return this; }
        public BookIssueBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public BookIssueBuilder returnDate(LocalDate returnDate) { this.returnDate = returnDate; return this; }
        public BookIssueBuilder fineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; return this; }
        public BookIssueBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public BookIssue build() {
            BookIssue bookIssue = new BookIssue(id, bookId, studentId, issueDate, dueDate, returnDate, fineAmount);
            if (tenantId != null) {
                bookIssue.setTenantId(tenantId);
            }
            return bookIssue;
        }
    }
}
