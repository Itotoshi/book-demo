package com.example.demo.controller

import com.example.demo.dto.BookResponse
import com.example.demo.dto.CreateBookRequest
import com.example.demo.dto.UpdateBookRequest
import com.example.demo.service.BookService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {
    /**
     * 書籍を新規登録
     */
    @PostMapping
    fun createBook(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse> {
        val book = bookService.createBook(request)
        return ResponseEntity.ok(book)
    }

    /**
     * 書籍を更新
     */
    @PatchMapping("/{id}")
    fun patchBook(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdateBookRequest
    ): ResponseEntity<BookResponse> {
        val updated = bookService.patchBook(id, request)
        return ResponseEntity.ok(updated)
    }

    /**
     * 著者IDから書籍一覧を取得
     */
    @GetMapping("/by-author/{authorId}")
    fun getBooksByAuthor(@PathVariable authorId: Int): ResponseEntity<List<BookResponse>> {
        val books = bookService.getBooksByAuthor(authorId)
        return ResponseEntity.ok(books)
    }
}