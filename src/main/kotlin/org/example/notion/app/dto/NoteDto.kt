package org.example.notion.app.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.LocalDateTime

//TODO тут надо еще подумать какие ограничения
data class NoteDto(
    val noteId: Int?,
    val owner: Int?,
    @NotNull
    @Size(max = 255)
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
): Serializable