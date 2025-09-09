package com.example.demo.service

import com.example.demo.dto.BookResponse
import com.example.demo.dto.CreateBookRequest
import com.example.demo.dto.UpdateBookRequest
import com.example.demo.enums.PublicationStatus
import com.example.jooq.Tables.BOOKS
import com.example.jooq.tables.BookAuthors.BOOK_AUTHORS

import org.jooq.DSLContext
import org.jooq.UpdateSetMoreStep
import org.springframework.stereotype.Service

@Service
class BookService(private val dsl: DSLContext) {
    /**
     * 書籍を新規登録
     */
    fun createBook(request: CreateBookRequest): BookResponse {
        val bookRecord = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, request.title)
            .set(BOOKS.PRICE, request.price)
            .set(BOOKS.STATUS, request.status.name)
            .returning()
            .fetchOne()!!
        val bookId = bookRecord.id!!

        // 中間テーブルに著者を紐付け
        request.authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }

        return BookResponse(
            id = bookId,
            title = bookRecord.title!!,
            price = bookRecord.price!!,
            authorIds = request.authorIds,
            status = PublicationStatus.valueOf(bookRecord.status!!)
        )
    }

    /**
     * 書籍を更新
     */
    fun patchBook(bookId: Int, request: UpdateBookRequest): BookResponse {
        val process = dsl.update(BOOKS).apply {
            request.title?.let { this.set(BOOKS.TITLE, it) }
            request.price?.let { this.set(BOOKS.PRICE, it) }
            request.status?.let {
                val currentStatus = dsl.select(BOOKS.STATUS)
                    .from(BOOKS)
                    .where(BOOKS.ID.eq(bookId))
                    .fetchOne(BOOKS.STATUS)!!
                if (currentStatus == PublicationStatus.PUBLISHED.name && it == PublicationStatus.UNPUBLISHED) {
                    // PUBLISHEDからUNPUBLISHEDへの変更は不可
                    throw IllegalStateException("Cannot change status from PUBLISHED to UNPUBLISHED")
                }
                this.set(BOOKS.STATUS, it.name)
            }
        }

        if (process is UpdateSetMoreStep<*>) { // 更新対象がある場合のみ実行
            val record = process
                .where(BOOKS.ID.eq(bookId))
                .returning(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
                .fetchOne()!!

            // 著者リスト更新
            request.authorIds?.let { newAuthors ->
                if (newAuthors.isEmpty()) {
                    throw IllegalArgumentException("authorIds は空にできません")
                }

                // 既存著者削除
                dsl.deleteFrom(BOOK_AUTHORS)
                    .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
                    .execute()

                // 新しい著者を追加
                newAuthors.forEach { authorId ->
                    dsl.insertInto(BOOK_AUTHORS)
                        .set(BOOK_AUTHORS.BOOK_ID, bookId)
                        .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                        .execute()
                }
            }

            // レスポンスに記載するために更新後の著者リストを取得
            val authorIds = dsl.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .fetch { it.value1() }

            return BookResponse(
                id = record.get(BOOKS.ID)!!,
                title = record.get(BOOKS.TITLE)!!,
                price = record.get(BOOKS.PRICE)!!,
                authorIds = authorIds,
                status = PublicationStatus.valueOf(record.get(BOOKS.STATUS)!!)
            )
        } else {
            throw IllegalArgumentException("更新する値が1つもありません")
        }
    }

}
