package org.example.notion.app.note.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable

data class NoteCreateDto(
    @NotBlank
    @Size(max = 255)
    val title: String,

    val description: String?

) : Serializable