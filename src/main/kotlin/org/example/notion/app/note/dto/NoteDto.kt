package org.example.notion.app.note.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.LocalDateTime

data class NoteDto(
    @Min(1)
    @NotNull
    val noteId: Long,

    @Min(1)
    @NotNull
    val owner: Long,

    @NotBlank
    @Size(max = 255)
    val title: String,

    val description: String?,

    @NotNull
    val createdAt: LocalDateTime,

    @NotNull
    val updatedAt: LocalDateTime
) : Serializable