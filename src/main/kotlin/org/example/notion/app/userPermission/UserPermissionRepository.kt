package org.example.notion.app.userPermission

import org.example.notion.app.userPermission.entity.NoteUserPermission
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface UserPermissionRepository : CrudRepository<NoteUserPermission, Long> {

    @Modifying
    @Query("delete from note_user_permission where user_id = :userId and note_id = :noteId")
    fun deleteNoteUserPermissionByUserIdAndNoteId(userId: String, noteId: Long)

    @Modifying
    @Query("delete from note_user_permission where note_id = :noteId")
    fun deleteNoteUserPermissionByNoteId(noteId: Long)

    fun findNoteUserPermissionByUserIdAndNoteId(userId: String, noteId: Long): NoteUserPermission?

    fun findAllByNoteId(noteId: Long) : List<NoteUserPermission>
}