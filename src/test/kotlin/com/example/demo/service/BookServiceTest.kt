package com.example.demo.service

import com.example.demo.dto.CreateBookRequest
import com.example.demo.dto.UpdateBookRequest
import com.example.demo.enums.PublicationStatus
import com.example.jooq.Tables.BOOKS
import com.example.jooq.tables.BookAuthors.BOOK_AUTHORS
import com.example.jooq.tables.records.BooksRecord
import io.mockk.*
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.RecordMapper
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
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

    /**
     * 書籍登録 正常系
     */
    @Test
    fun `createBook success`() {
        // リクエストデータを準備
        val request = CreateBookRequest(
            "title",
            1000.toBigDecimal(),
            listOf(1, 2),
            PublicationStatus.valueOf("PUBLISHED")  // APIをリクエストする際は"PUBLISHED"のように文字列で送られてくる想定のため、Enum.valueOf()で変換して指定
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

        // book_authorsテーブルへのinsertのモック化
        // execute()が1を返す（成功した件数）
        every {
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, any<Int>())
                .set(BOOK_AUTHORS.AUTHOR_ID, any<Int>())
                .execute()
        } returns 1

        // 登録された著者IDを取得するselect文のモック化
        every {
            dsl.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(1))
                .fetch(BOOK_AUTHORS.AUTHOR_ID)
        } returns listOf(1, 2)

        // 実際にサービスを呼び出す
        val result = service.createBook(request)

        // 戻り値が期待通りになっているか確認する
        // 今回はリクエストパラメータの値がそのまま返ってくる想定であるが、レスポンスはあくまでリクエストパラメータとは別物であることを明示するために変数化せずに値で記載する
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

    /**
     * 書籍更新 異常系 変更がない場合
     */
    @Test
    fun `patchBook fails when not changed value`() {
        val bookId = 1
        val request = UpdateBookRequest(
            title = null,
            price = null,
            status = null,
            authorIds = null
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
        every {
            dsl.selectFrom(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .fetchOne()
        } returns bookRecord

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.patchBook(bookId, request)
        }
        assertEquals("{patch.no.changes.exception}", exception.message)
    }

    /**
     * 書籍更新 異常系 authorIdsが空
     */
    @Test
    fun `patchBook fails when authorIds are empty`() {
        val bookId = 1
        val request = UpdateBookRequest(
            title = null,
            price = null,
            status = null,
            authorIds = emptyList()
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
        every {
            dsl.selectFrom(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .fetchOne()
        } returns bookRecord

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.patchBook(bookId, request)
        }
        assertEquals("authorIdsは空にできません", exception.message)
    }

    /**
     * 書籍更新 異常系 PUBLISHEDからUNPUBLISHEDへの変更はできない
     */
    @Test
    fun `patchBook fails when status PUBLISHED to UNPUBLISHED`() {
        val bookId = 1
        val request = UpdateBookRequest(
            title = null,
            price = null,
            status = PublicationStatus.UNPUBLISHED,
            authorIds = null
        )

        // 更新対象の書籍の出版状況がPUBLISHEDである場合のモック化
        every {
            dsl.select(BOOKS.STATUS).from(BOOKS).where(BOOKS.ID.eq(bookId)).fetchOne(BOOKS.STATUS)
        } returns PublicationStatus.PUBLISHED.name

        val exception = assertThrows(IllegalStateException::class.java) {
            service.patchBook(bookId, request)
        }
        assertEquals("PUBLISHEDはUNPUBLISHEDに変更できません", exception.message)
    }

    /**
     * 著者IDから書籍リストを取得する 正常系 複数件取得
     */
    @Test
    fun `getBooksByAuthor success when returns multiple books`() {
        val authorId = 10
        // 1冊目のRecord
        val record1 = BooksRecord().apply {
            id = 1
            title = "本1"
            price = 1000.toBigDecimal()
            status = PublicationStatus.PUBLISHED.name
        }
        // 2冊目のRecord
        val record2 = BooksRecord().apply {
            id = 2
            title = "本2"
            price = 2000.toBigDecimal()
            status = PublicationStatus.UNPUBLISHED.name
        }

        //JOIN結果のモック化
        val records = DSL.using(DefaultConfiguration()).newResult(BOOKS).apply {
            add(record1)
            add(record2)
        }
        every {
            dsl.select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
                .from(BOOKS)
                .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
                .fetchInto(BOOKS)
        } returns records

        // 各本に紐づく著者リスト
        every {
            dsl.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(1))
                .fetch(any<RecordMapper<Record1<Int>, Int>>())
        } returns listOf(authorId)

        every {
            dsl.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(2))
                .fetch(any<RecordMapper<Record1<Int>, Int>>())
        } returns listOf(authorId)

        // 実行
        val result = service.getBooksByAuthor(authorId)
        // 結果確認 2件取得できていること
        assertEquals(2, result.size)

        val book1 = result[0]
        assertEquals(1, book1.id)
        assertEquals("本1", book1.title)
        assertEquals(1000.toBigDecimal(), book1.price)
        assertEquals(listOf(authorId), book1.authorIds)
        assertEquals(PublicationStatus.PUBLISHED, book1.status)

        val book2 = result[1]
        assertEquals(2, book2.id)
        assertEquals("本2", book2.title)
        assertEquals(2000.toBigDecimal(), book2.price)
        assertEquals(listOf(authorId), book2.authorIds)
        assertEquals(PublicationStatus.UNPUBLISHED, book2.status)
    }

    /**
     * 著者IDから書籍リストを取得する 正常系 0件
     */
    @Test
    fun `getBooksByAuthor success when no books`() {
        val authorId = 99   // 存在しない著者ID

        val emptyRecords = dsl.newResult(BOOKS)

        every {
            dsl.select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
                .from(BOOKS)
                .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
                .fetchInto(BOOKS)
        } returns emptyRecords

        val result = service.getBooksByAuthor(authorId)

        assertTrue(result.isEmpty())
    }
}
