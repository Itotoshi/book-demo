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
import org.springframework.transaction.annotation.Transactional

@Transactional
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

        // 中間テーブルに著者を紐付け
        request.authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookRecord.get(BOOKS.ID)!!)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }
        // レスポンス作成用に登録された著者IDを取得
        val registeredAuthorIds = dsl.select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookRecord.get(BOOKS.ID)!!))
            .fetch(BOOK_AUTHORS.AUTHOR_ID)

        return BookResponse(
            id = bookRecord.get(BOOKS.ID)!!,
            title = bookRecord.get(BOOKS.TITLE)!!,
            price = bookRecord.get(BOOKS.PRICE)!!,
            authorIds = registeredAuthorIds,
            status = PublicationStatus.valueOf(bookRecord.get(BOOKS.STATUS)!!)
        )
    }

    /**
     * 書籍を更新
     */
    fun patchBook(bookId: Int, request: UpdateBookRequest): BookResponse {
        //booksテーブル更新用データの準備
        var hasChanges = false   //JOOQのテーブルUpdate用に使うDSLの作成フェーズを表す「UpdateSetMoreStep<*>」型を用いた更新有無の判定を想定していたが、うまく動作しないため専用の変数を用意
        val process = dsl.update(BOOKS)
            .apply {
                request.title?.let {
                    this.set(BOOKS.TITLE, it)
                    hasChanges = true
                }
                request.price?.let {
                    this.set(BOOKS.PRICE, it)
                    hasChanges = true
                }
                request.status?.let {
                    val currentStatus = dsl.select(BOOKS.STATUS)
                        .from(BOOKS)
                        .where(BOOKS.ID.eq(bookId))
                        .fetchOne(BOOKS.STATUS) ?: throw IllegalArgumentException("指定されたIDの書籍が存在しません")
                    if (currentStatus == PublicationStatus.PUBLISHED.name && it == PublicationStatus.UNPUBLISHED) {
                        // PUBLISHEDからUNPUBLISHEDへの変更は不可
                        throw IllegalStateException("${currentStatus}は${it.name}に変更できません")
                    }
                    this.set(BOOKS.STATUS, it.name)
                    hasChanges = true
                }
            }
        // booksテーブル更新＆レスポンス用の値を取得する
        val record = if (hasChanges && process is UpdateSetMoreStep<*>) {
            // 更新対象がある場合のみ UPDATE実行＆更新後の値を取得
            process.where(BOOKS.ID.eq(bookId))
                .returning(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
                .fetchOne() ?: throw IllegalArgumentException("指定されたIDの書籍が存在しません")
        } else {
            // 更新対象がなければ現在の値を取得
            dsl.selectFrom(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .fetchOne() ?: throw IllegalArgumentException("指定されたIDの書籍が存在しません")
        }


        // 著者リスト更新
        // 著者は最低1人必須のため、空リストの場合はエラー
        request.authorIds?.let { newAuthors ->
            if (newAuthors.isEmpty()) {
                throw IllegalArgumentException("authorIdsは空にできません")
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
        // 更新後の著者リストを取得
        val authorIds = dsl.select(BOOK_AUTHORS.AUTHOR_ID)
            .from(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetch { it.value1() }

        //レスポンスの返却
        if (hasChanges || !request.authorIds.isNullOrEmpty()) {
            // 更新対象がある場合のみレスポンスを返す
            return BookResponse(
                id = record.get(BOOKS.ID)!!,
                title = record.get(BOOKS.TITLE)!!,
                price = record.get(BOOKS.PRICE)!!,
                authorIds = authorIds,
                status = PublicationStatus.valueOf(record.get(BOOKS.STATUS)!!)
            )
        } else {
            throw IllegalArgumentException("{patch.no.changes.exception}")
        }
    }

    /**
     * 書籍を著者IDで検索
     */
    fun getBooksByAuthor(authorId: Int): List<BookResponse> {
        // 本と中間テーブルをJOINして著者の本を取得
        val records = dsl.select(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.STATUS)
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch()

        // 各本に紐づく著者リストを取得
        return records.map { record ->
            val bookId = record[BOOKS.ID]!!
            val authorIds = dsl.select(BOOK_AUTHORS.AUTHOR_ID)
                .from(BOOK_AUTHORS)
                .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .fetch { it.value1() }

            BookResponse(
                id = bookId,
                title = record[BOOKS.TITLE]!!,
                price = record[BOOKS.PRICE]!!,
                authorIds = authorIds,
                status = PublicationStatus.valueOf(record[BOOKS.STATUS]!!)
            )
        }
    }
}

