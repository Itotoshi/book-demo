package com.example.demo.controller

import com.example.demo.dto.CreateAuthorRequest
import com.example.demo.dto.UpdateAuthorRequest
import com.example.demo.service.AuthorService
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
@RequestMapping("/api/authors")
class AuthorController(private val authorService: AuthorService) {

    @PostMapping
    fun createAuthor(@Valid @RequestBody request: CreateAuthorRequest): ResponseEntity<Any> {
        val author = authorService.createAuthor(request)
        return ResponseEntity.status(201).body(author)
    }

    @PatchMapping("/{id}")
    fun patchAuthor(
        @PathVariable id: Int,
        @RequestBody request: UpdateAuthorRequest
    ): ResponseEntity<Any> {
        val author = authorService.patchAuthor(id, request)
        return if (author != null) ResponseEntity.ok(author)
        else ResponseEntity.notFound().build()
    }

    @GetMapping("/{id}")
    fun selectAuthor(@PathVariable id: Int): ResponseEntity<Any> {
        val author = authorService.selectAuthor(id)
        return if (author != null) ResponseEntity.ok(author)
        else ResponseEntity.notFound().build()
    }
}