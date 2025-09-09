package com.example.demo.dto

import com.example.demo.enums.PublicationStatus
import java.math.BigDecimal

/**
 * 書籍レスポンス
 */
data class BookResponse(
    val id: Int,
    val title: String,
    val price: BigDecimal,
    val authorIds: List<Int>,
    val status: PublicationStatus
)