package org.example.notion.app.userPermission

import org.example.notion.app.userPermission.entity.NoteTeamPermission
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TeamPermissionRepository : CrudRepository<NoteTeamPermission, Long> {

    fun findNoteUserPermissionByTeamIdAndNoteId(teamId: Long, noteId: Long): Optional<NoteTeamPermission>
    fun deleteNoteUserPermissionByTeamIdAndNoteId(teamId: Long, noteId: Long)
    fun findAllByNoteId(noteId: Long): List<NoteTeamPermission>

}