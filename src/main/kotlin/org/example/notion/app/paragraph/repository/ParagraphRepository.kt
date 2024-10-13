package org.example.notion.app.paragraph.repository

import org.example.notion.app.paragraph.entity.Paragraph
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface ParagraphRepository : CrudRepository<Paragraph, Long> {

    @Query("select * from paragraph where (next_paragraph_id = :nextParagraphId) and (note_id = :noteId)")
    fun findByNextParagraphIdAndNoteId(nextParagraphId: Long, noteId: Long): Paragraph?

    @Query("select * from paragraph where next_paragraph_id is null and note_id = :noteId")
    fun findByNextParagraphIdNullAndNoteId(noteId: Long): Paragraph?

    @Query("select * from paragraph where id = :id")
    fun findByParagraphId(id: Long): Paragraph?

    @Query("select * from paragraph where id = :paragraphId and note_id = :noteId")
    fun findByParagraphIdAndNoteId(paragraphId: Long, noteId: Long): Paragraph?

    @Query("SELECT * FROM paragraph LIMIT :pageSize OFFSET :offset")
    fun findAllParagraphs(pageSize: Long, offset: Long): List<Paragraph>
}