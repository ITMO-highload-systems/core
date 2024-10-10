package org.example.notion.app.paragraph.repository

import org.example.notion.app.paragraph.entity.Paragraph
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface ParagraphRepository : CrudRepository<Paragraph, Long> {

    @Query("select * from paragraph where note_id = :noteId")
    fun findByNoteId(noteId: Long): List<Paragraph>

    @Query("select * from paragraph where note_id = :noteId and id = :id")
    fun findByNoteIdAndParagraphId(noteId: Long, id: Long): Paragraph?

    @Query("select * from paragraph where (next_paragraph_id = :nextParagraphId)")
    fun findByNextParagraphId(nextParagraphId: Long): Paragraph?

    @Query("select * from paragraph where next_paragraph_id is null")
    fun findByNextParagraphIdNull(): Paragraph?

    @Query("select * from paragraph where id = :id")
    fun findByParagraphId(id: Long): Paragraph?

    @Query("SELECT * FROM paragraph LIMIT :pageSize OFFSET :offset")
    fun findAllParagraphs(pageSize: Long, offset: Long): List<Paragraph>
}