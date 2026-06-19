package com.saas.smartcampus.library.controller;

import com.saas.smartcampus.library.dto.IssueRequest;
import com.saas.smartcampus.library.entity.Book;
import com.saas.smartcampus.library.entity.BookIssue;
import com.saas.smartcampus.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService service;

    public LibraryController(LibraryService service) {
        this.service = service;
    }

    @PostMapping("/books")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book created = service.addBook(book);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(service.getAllBooks());
    }

    @PostMapping("/issue")
    public ResponseEntity<BookIssue> issueBook(@RequestBody IssueRequest request) {
        BookIssue issue = service.issueBook(request.getBookId(), request.getStudentId());
        return new ResponseEntity<>(issue, HttpStatus.CREATED);
    }

    @PostMapping("/return/{issueId}")
    public ResponseEntity<BookIssue> returnBook(@PathVariable Long issueId) {
        BookIssue returned = service.returnBook(issueId);
        return ResponseEntity.ok(returned);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<BookIssue>> getIssuesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getIssuesByStudent(studentId));
    }
}
