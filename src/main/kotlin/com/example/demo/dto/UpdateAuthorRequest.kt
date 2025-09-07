package com.example.demo.dto

import java.time.LocalDate

data class UpdateAuthorRequest(
    val name: String?,
    val birthDay: LocalDate?
)
