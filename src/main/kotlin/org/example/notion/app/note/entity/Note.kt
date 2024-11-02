package org.example.notion.app.note.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("note")
data class Note(
    @Id
    val noteId: Long,
    val owner: String,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)