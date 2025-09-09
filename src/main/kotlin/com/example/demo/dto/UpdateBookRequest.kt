package com.example.demo.dto

import com.example.demo.enums.PublicationStatus
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

/**
 * 書籍更新リクエスト
 */
data class UpdateBookRequest(
    val title: String? = null,

    @field:PositiveOrZero(message = "{book.price.positiveOrZero}")
    val price: BigDecimal? = null,

    val authorIds: List<Int>? = null,

    val status: PublicationStatus? = null

)
