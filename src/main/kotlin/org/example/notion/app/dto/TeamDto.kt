package org.example.notion.app.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.io.Serializable

data class TeamDto(
    @Min(1)
    @NotNull
    val teamId: Int,

    @NotNull
    @NotBlank
    @Size(max = 255)
    val name: String,

    @Min(1)
    @NotNull
    val owner: Int
) : Serializable