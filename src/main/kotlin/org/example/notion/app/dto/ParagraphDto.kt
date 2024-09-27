package org.example.notion.app.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ParagraphDto(
    @NotNull
    @Min(1)
    val paragraphId: Int,

    @NotNull
    @Min(1)
    val noteId: Int,

    @Size(max = 255)
    val title: String,

    @NotNull
    val position: Int,

    @NotNull
    val text: String,

    @NotNull
    @Min(1)
    val lastUpdateUserId: Int,

    @NotNull
    val createdAt: LocalDateTime,

    @NotNull
    val updatedAt: LocalDateTime,

    @Size(max = 3)
    val paragraphType: String
)