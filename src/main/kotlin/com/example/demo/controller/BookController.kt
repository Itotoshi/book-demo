package com.example.demo.controller

import com.example.demo.dto.BookResponse
import com.example.demo.dto.CreateBookRequest
import com.example.demo.dto.UpdateBookRequest
import com.example.demo.service.BookService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {
    @PostMapping
    fun createBook(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse> {
        val book = bookService.createBook(request)
        return ResponseEntity.ok(book)
    }
    @PatchMapping("/{id}")
    fun patchBook(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateBookRequest
    ): ResponseEntity<BookResponse> {
        val updated = bookService.patchBook(id, request)
        return ResponseEntity.ok(updated)
    }
}