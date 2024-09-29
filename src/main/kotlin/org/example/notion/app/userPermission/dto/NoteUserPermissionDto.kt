package org.example.notion.app.userPermission.dto

import org.example.notion.app.userPermission.entity.Permission

data class NoteUserPermissionDto (
    val userId:Long,
    val noteId:Long,
    val permission: Permission
)