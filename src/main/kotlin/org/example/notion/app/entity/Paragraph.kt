package org.example.notion.app.entity

import java.time.LocalDateTime

data class Paragraph(
    val paragraphId: Int,
    val noteId: Int,
    val title: String,
    val position: Int,
    val text: String,
    val lastUpdateUserId: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val paragraphType: String
)