package org.example.notion.app.userPermission.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class NoteTeamPermissionDeleteDto(
    val teamId: Long,
    val noteId: Long
)