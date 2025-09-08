package com.example.demo.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class CreateAuthorRequest (
    @field:NotBlank
    val name: String,

    @field:Past
    val birthday: LocalDate
)