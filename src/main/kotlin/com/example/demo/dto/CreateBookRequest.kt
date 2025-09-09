package com.example.demo.dto

import com.example.demo.enums.PublicationStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

/**
 * 書籍作成リクエスト
 */
data class CreateBookRequest(
    @field:NotBlank(message = "{book.title.notBlank}")
    val title: String,

    @field:PositiveOrZero(message = "{book.price.positiveOrZero}")
    val price: BigDecimal,

    @field:NotEmpty
    val authorIds: List<Int>,

    val status: PublicationStatus = PublicationStatus.UNPUBLISHED
)
