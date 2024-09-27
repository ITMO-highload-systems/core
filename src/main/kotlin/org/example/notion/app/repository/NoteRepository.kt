package org.example.notion.app.repository

import org.example.notion.app.dto.NoteDto
import org.example.notion.app.entity.Note
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Component

@Component
class NoteRepository(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
) {

    companion object {
        private const val SELECT_FROM_NOTE =
            """
        select 
          note_id,
          owner,
          title,
          description,
          created_at,
          updated_at
        from note
      """
        private const val FIND_NOTE_BY_NOTE_ID = "$SELECT_FROM_NOTE where note_id = :note_id"
        private const val FIND_NOTES_BY_OWNER = "$SELECT_FROM_NOTE where owner = :owner"

        private const val UPDATE_TITLE =
            "update note set title = :new_title, updated_at = now() where note_id = :note_id"
        private const val UPDATE_DESCRIPTION =
            "update note set description = :new_description, updated_at = now() where note_id = :note_id"
        private const val UPDATE_NOTE =
            "update note set title = :new_title, description = :new_description, updated_at = now() where note_id = :note_id"

        private const val DELETE_BY_NOTE_ID = "delete from note where note_id = :note_id"
        private const val DELETE_BY_OWNER = "delete from note where owner = :owner"

        private const val INSERT_INTO_NOTE =
            """
        insert into note (
          owner,
          title,
          description   
        ) 
        values (
          :owner,
          :title,
          :description
        )
      """
    }

    private val rowMapper: RowMapper<Note> = RowMapper { rs, _ ->
        Note(
            noteId = rs.getInt("note_id"),
            owner = rs.getString("owner").toInt(),
            title = rs.getString("title"),
            description = rs.getString("description"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
        )
    }

    fun findByNoteId(noteId: Int): Note? =
        namedParameterJdbcOperations.query(
            FIND_NOTE_BY_NOTE_ID,
            mapOf("note_id" to noteId),
            rowMapper
        ).singleOrNull()

    fun findByOwner(owner: Int): List<Note> =
        namedParameterJdbcOperations.query(
            FIND_NOTES_BY_OWNER,
            mapOf("owner" to owner),
            rowMapper
        )

    fun updateNote(noteDto: NoteDto): Int =
        namedParameterJdbcOperations.update(
            UPDATE_NOTE,
            mapOf(
                "note_id" to noteDto.noteId,
                "description" to noteDto.description,
                "title" to noteDto.title
            )
        )

    fun deleteByNoteId(noteId: Int): Int =
        namedParameterJdbcOperations.update(
            DELETE_BY_NOTE_ID,
            mapOf("note_id" to noteId)
        )

    fun save(noteDto: NoteDto): Int =
        namedParameterJdbcOperations.update(
            INSERT_INTO_NOTE,
            mapOf(
                "owner" to noteDto.owner,
                "title" to noteDto.title,
                "description" to noteDto.description
            )
        )

}
