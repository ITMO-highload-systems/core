package org.example.notion.app.team.entity

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "team")
data class Team(
    @Id
    val teamId: Long,
    @field:NotBlank
    val name: String,
    val owner: Long
)