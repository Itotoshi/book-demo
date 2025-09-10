package com.example.demo.service

import com.example.demo.dto.CreateBookRequest
import com.example.demo.enums.PublicationStatus
import com.example.jooq.Tables.BOOKS
import com.example.jooq.tables.BookAuthors.BOOK_AUTHORS
import com.example.jooq.tables.records.BooksRecord
import io.mockk.*
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BookServiceTest {
    private lateinit var dsl: DSLContext
    private lateinit var service: BookService

    @BeforeEach
    fun setup() {
        dsl = mockk(relaxed = true)
        service = BookService(dsl)
    }

    @Test
    fun `createBook success`() {
        //リクエストデータを準備
        val request = CreateBookRequest(
            "title",
            1000.toBigDecimal(),
            listOf(1, 2),
            PublicationStatus.valueOf("PUBLISHED")  //APIをリクエストする際は"PUBLISHED"のように文字列で送られてくる想定のため、Enum.valueOf()で変換して指定
        )

        // jOOQが返すBooksRecordをモック化
        // returning().fetchOne()で1つ返ってくる想定
        val bookRecord = mockk<BooksRecord>(relaxed = true) {
            // 各カラムが返す値を設定
            every { get(BOOKS.ID) } returns 1
            every { get(BOOKS.TITLE) } returns "title"
            every { get(BOOKS.PRICE) } returns 1000.toBigDecimal()
            every { get(BOOKS.STATUS) } returns "PUBLISHED"
        }

        // DSLContextのinsertInto(BOOKS)...の呼び出しをモック
        // set()で指定した値が正しく入るかをチェックするため、引数をrequestから指定
        every {
            dsl.insertInto(BOOKS)
                .set(BOOKS.TITLE, request.title)
                .set(BOOKS.PRICE, request.price)
                .set(BOOKS.STATUS, request.status.name)
                .returning()
                .fetchOne()
        } returns bookRecord

        // book_authorsテーブルへのinsertもモック化
        // execute()が1を返す（成功した件数）
        every {
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, any<Int>())
                .set(BOOK_AUTHORS.AUTHOR_ID, any<Int>())
                .execute()
        } returns 1

        // 実際にサービスを呼び出す
        val result = service.createBook(request)

        // 戻り値が期待通りになっているか確認する
        // 今回はリクエストパラメータの値がそのまま返ってくる想定であるが、レスポンスはあくまでリクエストパラメータとは別物であることを明示するために変数化せずに値を記載する
        assertEquals(1, result.id)                                                   // DBに登録されたID
        assertEquals("title", result.title)                                          // タイトル
        assertEquals(1000.toBigDecimal(), result.price)                              // 価格
        assertEquals(listOf(1, 2), result.authorIds)                                 // 著者IDリスト
        assertEquals(PublicationStatus.valueOf("PUBLISHED"), result.status)   // 出版ステータス

        // 著者が2人分insertされたことを確認する
        verify(exactly = 2) {
            dsl.insertInto(BOOK_AUTHORS)
        }
    }
}
