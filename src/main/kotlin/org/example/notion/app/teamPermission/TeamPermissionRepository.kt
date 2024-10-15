package org.example.notion.app.userPermission

import org.example.notion.app.userPermission.entity.NoteTeamPermission
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface TeamPermissionRepository : CrudRepository<NoteTeamPermission, Long> {

    fun findNoteTeamPermissionByTeamIdAndNoteId(teamId: Long, noteId: Long): NoteTeamPermission?

    @Modifying
    @Query("DELETE FROM note_team_permission WHERE note_id = :noteId AND team_id = :teamId")
    fun deleteNoteUserPermissionByTeamIdAndNoteId(teamId: Long, noteId: Long)

    @Modifying
    @Query("DELETE FROM note_team_permission WHERE note_id = :noteId")
    fun deleteNoteUserPermissionByNoteId(noteId: Long)
    fun findAllByNoteId(noteId: Long): List<NoteTeamPermission>
    fun findAllByTeamId(noteId: Long): List<NoteTeamPermission>

}