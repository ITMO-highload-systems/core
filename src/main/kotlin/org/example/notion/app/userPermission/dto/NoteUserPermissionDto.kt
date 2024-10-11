package org.example.notion.app.userPermission.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.example.notion.app.userPermission.entity.Permission

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class NoteUserPermissionDto (
    val userId:Long,
    val noteId:Long,
    val permission: Permission
)