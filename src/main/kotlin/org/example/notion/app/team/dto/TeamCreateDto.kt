package org.example.notion.app.team.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.io.Serializable

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamCreateDto(

    @NotBlank
    @Size(max = 255)
    val name: String,

    @Min(1)
    @NotNull
    val owner: Long
) : Serializable