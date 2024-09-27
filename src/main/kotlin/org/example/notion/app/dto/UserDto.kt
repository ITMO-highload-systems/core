package org.example.notion.app.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserDto(
    @Min(1)
    @NotNull
    val userId: Int,

    @NotNull
    @NotBlank
    @Email
    val email: String,

    @NotBlank
    @NotNull
    val password: String
)