package org.example.notion.app.paragraph.repository

import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.entity.Paragraph
import org.example.notion.app.paragraph.entity.ParagraphType
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Component

@Component
class ParagraphRepository(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
) {

    companion object {
        private const val SELECT_FROM_PARAGRAPH = """
            select
                paragraph_id,
                note_id,
                title,
                next_parapgraph_id,
                text,
                last_update_user_id,
                created_at,
                updated_at,
                paragraph_type
            from paragraph
        """

        private const val FIND_BY_PARAGRAPH_ID = "$SELECT_FROM_PARAGRAPH where paragraph_id = :paragraph_id;"
        private const val FIND_BY_NOTE_ID = "$SELECT_FROM_PARAGRAPH where note_id = :note_id;"
        private const val FIND_BY_LAST_UPDATE_USER_ID = "$SELECT_FROM_PARAGRAPH where last_update_user_id = :last_update_user_id;"
        private const val FIND_NEXT_PARAGRAPH_ID = "$SELECT_FROM_PARAGRAPH where next_paragraph_id = :next_paragraph_id;"

        private const val UPDATE_PARAGRAPH = """
            update paragraph 
            set
                title = :title,
                next_parapgraph_id = :next_parapgraph_id,
                text = :text,
                last_update_user_id = :last_update_user_id,
                updated_at = now(),
                paragraph_type = :paragraph_type
            where
                paragraph_id = :paragraph_id;
        """

        private const val DELETE_BY_PARAGRAPH_ID = "delete from paragraph where paragraph_id = :paragraph_id;"

        private const val INSERT_INTO_PARAGRAPH = """
            insert into paragraph(
                note_id,
                title,
                next_paragraph_id,
                text,
                last_update_user_id,
                paragraph_type
            ) values (
                :note_id,
                :title,
                :next_paragraph_id,
                :text,
                :last_update_user_id,
                :paragraph_type
            );
        """
    }

    private val rowMapper: RowMapper<Paragraph> = RowMapper { rs, _ ->
        Paragraph(
            id = rs.getLong("paragraph_id"),
            noteId = rs.getLong("note_id"),
            title = rs.getString("title"),
            nextParagraphId = rs.getLong("next_paragraph_id"),
            text = rs.getString("text"),
            lastUpdateUserId = rs.getInt("last_update_user_id"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
            paragraphType = ParagraphType.valueOf(rs.getString("paragraph_type"))
        )
    }

    fun findByParagraphId(paragraphId: Long): Paragraph? =
        namedParameterJdbcOperations.query(
            FIND_BY_PARAGRAPH_ID,
            mapOf("paragraph_id" to paragraphId),
            rowMapper
        ).singleOrNull()

    fun findByNoteId(noteId: Int): List<Paragraph> =
        namedParameterJdbcOperations.query(
            FIND_BY_NOTE_ID,
            mapOf("note_id" to noteId),
            rowMapper
        )

    fun findByNextParagraphId(nextParagraphId: Long): Paragraph? =
        namedParameterJdbcOperations.query(
            FIND_NEXT_PARAGRAPH_ID,
            mapOf("next_paragraph_id" to nextParagraphId),
            rowMapper
        ).firstOrNull()

    fun deleteByParagraphId(paragraphId: Long): Int =
        namedParameterJdbcOperations.update(
            DELETE_BY_PARAGRAPH_ID,
            mapOf("paragraph_id" to paragraphId),
        )

    fun update(paragraph: Paragraph, lastUpdateUserId: Long): Paragraph =
        namedParameterJdbcOperations.queryForObject(
            UPDATE_PARAGRAPH,
            mapOf(
                "title" to paragraph.title,
                "text" to paragraph.text,
                "last_update_user_id" to lastUpdateUserId,
                "paragraph_type" to paragraph.paragraphType,
                "next_paragraph_id" to paragraph.nextParagraphId,
            ),
            rowMapper
        ) ?: throw IllegalStateException("Failed to update paragraph")

    fun save(paragraphCreateRequest: ParagraphCreateRequest, userId: Long): Paragraph =
        namedParameterJdbcOperations.queryForObject(
            INSERT_INTO_PARAGRAPH,
            mapOf(
                "note_id" to paragraphCreateRequest.noteId,
                "title" to paragraphCreateRequest.title,
                "next_paragraph_id" to paragraphCreateRequest.nextParagraphId,
                "text" to paragraphCreateRequest.text,
                "last_update_user_id" to userId,
                "paragraph_type" to paragraphCreateRequest.paragraphType.name,
            ),
            rowMapper
        ) ?: throw IllegalStateException("Failed to save paragraph")
}