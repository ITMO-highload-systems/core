package org.example.notion.app.repository

import org.example.notion.app.dto.ParagraphDto
import org.example.notion.app.entity.Paragraph
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
                position,
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

        private const val UPDATE_PARAGRAPH = """
            update paragraph 
            set
                title = :title,
                position = :position,
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
                position,
                text,
                last_update_user_id,
                paragraph_type
            ) values (
                :note_id,
                :title,
                :position,
                :text,
                :last_update_user_id,
                :paragraph_type
            );
        """
    }

    private val rowMapper: RowMapper<Paragraph> = RowMapper { rs, _ ->
        Paragraph(
            paragraphId = rs.getInt("paragraph_id"),
            noteId = rs.getInt("note_id"),
            title = rs.getString("title"),
            position = rs.getInt("position"),
            text = rs.getString("text"),
            lastUpdateUserId = rs.getInt("last_update_user_id"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime(),
            paragraphType = rs.getString("paragraph_type")

        )
    }

    fun findByParagraphId(paragraphId: Int): Paragraph? =
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

    fun deleteByParagraphId(paragraphId: Int): Int =
        namedParameterJdbcOperations.update(
            DELETE_BY_PARAGRAPH_ID,
            mapOf("paragraph_id" to paragraphId),
        )

    fun update(paragraphDto: ParagraphDto): Int =
        namedParameterJdbcOperations.update(
            UPDATE_PARAGRAPH,
            mapOf(
                "title" to paragraphDto.title,
                "position" to paragraphDto.position,
                "text" to paragraphDto.text,
                "last_update_user_id" to paragraphDto.lastUpdateUserId,
                "paragraph_type" to paragraphDto.paragraphType,
            )
        )

    fun save(paragraphDto: ParagraphDto): Int =
        namedParameterJdbcOperations.update(
            INSERT_INTO_PARAGRAPH,
            mapOf(
                "note_id" to paragraphDto.noteId,
                "title" to paragraphDto.title,
                "position" to paragraphDto.position,
                "text" to paragraphDto.text,
                "last_update_user_id" to paragraphDto.lastUpdateUserId,
                "paragraph_type" to paragraphDto.paragraphType,
            )
        )
}