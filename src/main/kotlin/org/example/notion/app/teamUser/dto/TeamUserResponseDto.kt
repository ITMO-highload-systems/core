package org.example.notion.app.teamUser.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamUserResponseDto(
    val id: Long,
    val teamId: Long,
    val userId: String
)