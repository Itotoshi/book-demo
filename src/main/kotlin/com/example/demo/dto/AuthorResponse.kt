package com.example.demo.dto

import java.time.LocalDate

data class AuthorResponse(
    val name: String,
    val birthday: LocalDate
)
