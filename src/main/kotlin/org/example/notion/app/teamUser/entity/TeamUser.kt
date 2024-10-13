package org.example.notion.app.teamUser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "team_user")
data class TeamUser(
    @Id
    val id: Long,
    val teamId: Long,
    val userId: Long
)