package com.example.demo.dto

import jakarta.validation.constraints.PositiveOrZero

data class UpdateBookRequest (
    val title: String?,

    @field:PositiveOrZero(message = "{book.price.positiveOrZero}")
    val price: Double?,

    val published: Boolean?,

    val authorIds: List<Int>?
)
