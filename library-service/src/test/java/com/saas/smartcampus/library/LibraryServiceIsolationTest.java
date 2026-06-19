package com.saas.smartcampus.library;

import com.saas.smartcampus.library.entity.Book;
import com.saas.smartcampus.library.entity.BookIssue;
import com.saas.smartcampus.library.repository.BookIssueRepository;
import com.saas.smartcampus.library.repository.BookRepository;
import com.saas.smartcampus.library.service.LibraryService;
import com.saas.smartcampus.shared.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LibraryServiceIsolationTest {

    @Autowired
    private LibraryService service;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookIssueRepository bookIssueRepository;

    @AfterEach
    public void cleanup() {
        TenantContext.setCurrentTenant("tenant-a");
        bookIssueRepository.deleteAll();
        bookRepository.deleteAll();
        TenantContext.setCurrentTenant("tenant-b");
        bookIssueRepository.deleteAll();
        bookRepository.deleteAll();
        TenantContext.clear();
    }

    @Test
    public void testTenantIsolationOnLibrary() {
        // 1. Add Book to Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        Book bookA = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .availableCopies(3)
                .build();
        service.addBook(bookA);

        // 2. Add Book to Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        Book bookB = Book.builder()
                .title("Design Patterns")
                .author("Gang of Four")
                .isbn("978-0201633610")
                .availableCopies(5)
                .build();
        service.addBook(bookB);

        // 3. Query as Tenant A
        TenantContext.setCurrentTenant("tenant-a");
        List<Book> listA = service.getAllBooks();
        assertEquals(1, listA.size());
        assertEquals("Clean Code", listA.get(0).getTitle());

        // 4. Query as Tenant B
        TenantContext.setCurrentTenant("tenant-b");
        List<Book> listB = service.getAllBooks();
        assertEquals(1, listB.size());
        assertEquals("Design Patterns", listB.get(0).getTitle());
    }

    @Test
    public void testLibraryBusinessRules() {
        TenantContext.setCurrentTenant("tenant-a");

        // 1. Create a Book
        Book book = Book.builder()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("978-0134685991")
                .availableCopies(1)
                .build();
        Book savedBook = service.addBook(book);

        // 2. Issue book and verify inventory decrement
        BookIssue issue = service.issueBook(savedBook.getId(), 500L);
        assertNotNull(issue);
        assertEquals(savedBook.getId(), issue.getBookId());
        assertEquals(500L, issue.getStudentId());
        
        Book bookAfterIssue = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(0, bookAfterIssue.getAvailableCopies()); // Decremented to 0

        // 3. Mock overdue book issue in repository to test fine calculation
        // Let's modify the due date to 5 days ago
        issue.setDueDate(LocalDate.now().minusDays(5));
        bookIssueRepository.saveAndFlush(issue);

        // 4. Return book and check inventory increment & fine calculation
        BookIssue returnedIssue = service.returnBook(issue.getId());
        assertNotNull(returnedIssue.getReturnDate());
        assertEquals(LocalDate.now(), returnedIssue.getReturnDate());
        
        // Overdue by 5 days, fine rate is $2.00/day -> total fine should be $10.00
        assertEquals(new BigDecimal("10.00"), returnedIssue.getFineAmount());

        Book bookAfterReturn = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(1, bookAfterReturn.getAvailableCopies()); // Incremented back to 1
    }
}
