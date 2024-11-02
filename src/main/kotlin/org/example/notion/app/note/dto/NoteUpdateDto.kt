package org.example.notion.app.note.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.io.Serializable

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class NoteUpdateDto(
    @Min(1)
    @NotNull
    val noteId: Long,

    @Min(1)
    @NotNull
    val owner: String,

    @NotBlank
    @Size(max = 255)
    val title: String,

    val description: String?,

    ) : Serializable