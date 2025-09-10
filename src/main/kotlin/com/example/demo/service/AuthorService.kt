package com.example.demo.service

import com.example.demo.dto.CreateAuthorRequest
import com.example.demo.dto.UpdateAuthorRequest
import com.example.demo.dto.AuthorResponse
import com.example.jooq.Tables.AUTHORS
import org.jooq.DSLContext
import org.jooq.UpdateSetMoreStep
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.InvalidParameterException

@Transactional
@Service
class AuthorService(private val dsl: DSLContext) {

    fun createAuthor(request: CreateAuthorRequest): AuthorResponse? {
        val record = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, request.name)
            .set(AUTHORS.BIRTHDAY, request.birthday)
            .returning(AUTHORS.NAME, AUTHORS.BIRTHDAY)
            .fetchOne()

        return record?.let {
            AuthorResponse(it.name!!, it.birthday!!)
        }
    }

    fun patchAuthor(id: Int, request: UpdateAuthorRequest): AuthorResponse? {
        val process = dsl.update(AUTHORS)
            .apply {
                request.name?.let { this.set(AUTHORS.NAME, it) }
                request.birthday?.let { this.set(AUTHORS.BIRTHDAY, it) }
            }
        if (process is UpdateSetMoreStep<*>) { // 更新する値が一つもない場合、UpdatSetMoreStepになっていないため更新を実行しない
            val record = process.where(AUTHORS.ID.eq(id))
                .returning(AUTHORS.NAME, AUTHORS.BIRTHDAY)
                .fetchOne()
            return record?.let {
                AuthorResponse(it.get(AUTHORS.NAME)!!, it.get(AUTHORS.BIRTHDAY)!!)
            }
        } else {
            throw InvalidParameterException("更新する値が一つもありません")
        }
    }


    fun selectAuthor(id: Int): AuthorResponse? {
        val record = dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()

        return record?.let {
            AuthorResponse(it.name!!, it.birthday!!)
        }
    }
}
