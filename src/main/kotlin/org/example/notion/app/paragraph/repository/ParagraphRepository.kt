package org.example.notion.app.paragraph.repository

import org.example.notion.app.paragraph.entity.Paragraph
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface ParagraphRepository : PagingAndSortingRepository<Paragraph, Long>, CrudRepository<Paragraph, Long> {

    @Query("select * from paragraph where note_id = :noteId")
    fun findByNoteId(noteId: Long): List<Paragraph>

    @Query("select * from paragraph where note_id = :noteId and id = :id")
    fun findByNoteIdAndParagraphId(noteId: Long, id: Long): Paragraph?

    @Query("select * from paragraph where (:nextParagraphId is null or next_paragraph_id = :nextParagraphId)")
    fun findByNextParagraphId(nextParagraphId: Long?): Paragraph?

    @Query("select * from paragraph where id = :id")
    fun findByParagraphId(id: Long): Paragraph?
}