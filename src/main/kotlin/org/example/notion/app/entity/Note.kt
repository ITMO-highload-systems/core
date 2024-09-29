package org.example.notion.app.entity

import java.time.LocalDateTime

data class Note(
    val noteId: Int,
    val owner: Int,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
