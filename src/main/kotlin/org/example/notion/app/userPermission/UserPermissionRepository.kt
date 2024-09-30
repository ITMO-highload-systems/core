package org.example.notion.app.userPermission

import org.example.notion.app.userPermission.entity.NoteUserPermission
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface UserPermissionRepository : CrudRepository<NoteUserPermission, Long> {

    @Modifying
    @Query("delete from note_user_permission where user_id = :userId and note_id = :noteId")
    fun deleteNoteUserPermissionByUserIdAndNoteId(userId: Long, noteId: Long)

    fun findNoteUserPermissionByUserIdAndNoteId(userId: Long, noteId: Long) : Optional<NoteUserPermission>

    fun findAllByNoteId(noteId: Long) : List<NoteUserPermission>
}