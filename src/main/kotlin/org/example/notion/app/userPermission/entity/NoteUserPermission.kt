package org.example.notion.app.userPermission.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "note_user_permission")
data class NoteUserPermission(
    @Id
    val permissionId: Long,
    val noteId: Long,
    val userId: Long,
    val permission: Permission,
)