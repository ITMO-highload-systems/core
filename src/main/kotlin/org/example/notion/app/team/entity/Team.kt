package org.example.notion.app.team.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "team")
data class Team(
    @Id
    val teamId: Long,
    val name: String,
    val owner: Long
)