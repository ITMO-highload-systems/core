package org.example.notion.app.teamUser.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamUserCreateDto(
    val teamId: Long,
    val userId: Long
)