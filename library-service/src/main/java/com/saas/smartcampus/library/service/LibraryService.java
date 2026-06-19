package com.saas.smartcampus.library.service;

import com.saas.smartcampus.library.entity.Book;
import com.saas.smartcampus.library.entity.BookIssue;
import com.saas.smartcampus.library.repository.BookIssueRepository;
import com.saas.smartcampus.library.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LibraryService {

    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);
    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("2.00");

    private final BookRepository bookRepository;
    private final BookIssueRepository bookIssueRepository;

    public LibraryService(BookRepository bookRepository, BookIssueRepository bookIssueRepository) {
        this.bookRepository = bookRepository;
        this.bookIssueRepository = bookIssueRepository;
    }

    @Transactional
    public Book addBook(Book book) {
        log.info("Adding book to catalog: title={}, isbn={}", book.getTitle(), book.getIsbn());
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional
    public BookIssue issueBook(Long bookId, Long studentId) {
        log.info("Attempting to issue book ID {} to student ID {}", bookId, studentId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book with ID " + bookId + " not found!"));

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' has no available copies left in inventory!");
        }

        // Decrement copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        BookIssue issue = BookIssue.builder()
                .bookId(bookId)
                .studentId(studentId)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .fineAmount(BigDecimal.ZERO)
                .build();

        return bookIssueRepository.save(issue);
    }

    @Transactional
    public BookIssue returnBook(Long issueId) {
        log.info("Processing return for book issue ID {}", issueId);
        BookIssue issue = bookIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Book issue record with ID " + issueId + " not found!"));

        if (issue.getReturnDate() != null) {
            throw new IllegalStateException("Book has already been returned!");
        }

        LocalDate today = LocalDate.now();
        issue.setReturnDate(today);

        // Re-increment book inventory
        Book book = bookRepository.findById(issue.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book associated with issue record not found!"));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // Fine calculation
        if (today.isAfter(issue.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(issue.getDueDate(), today);
            BigDecimal fine = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(daysOverdue));
            issue.setFineAmount(fine);
            log.info("Book return is overdue by {} days. Fine calculated: ${}", daysOverdue, fine);
        } else {
            issue.setFineAmount(BigDecimal.ZERO);
        }

        return bookIssueRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public List<BookIssue> getIssuesByStudent(Long studentId) {
        return bookIssueRepository.findByStudentId(studentId);
    }
}
