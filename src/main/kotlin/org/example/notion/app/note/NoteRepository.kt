package org.example.notion.app.note

import org.example.notion.app.note.entity.Note
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository


interface NoteRepository : CrudRepository<Note, Long> {

    @Query("SELECT * FROM note WHERE note_id = :noteId")
    fun findByNoteId(noteId: Long): Note?

    @Query("SELECT * FROM note WHERE owner = :owner")
    fun findByOwner(owner: String): List<Note>

    @Modifying
    @Query("DELETE FROM note WHERE note_id = :noteId")
    fun deleteByNoteId(noteId: Long)

}