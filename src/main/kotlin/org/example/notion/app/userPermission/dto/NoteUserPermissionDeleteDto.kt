package org.example.notion.app.userPermission.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class NoteUserPermissionDeleteDto (
    val userId: String,
    val noteId:Long
)