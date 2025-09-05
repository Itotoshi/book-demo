package com.example.demo.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero

data class CreateBookRequest (
    @field:NotBlank(message = "{book.title.notBlank}")
    val title: String,

    @field:PositiveOrZero(message = "{book.price.positiveOrZero}")
    val price: Double,

    @field:NotEmpty
    val authorIds: List<Int>
)
