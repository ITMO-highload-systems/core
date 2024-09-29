package org.example.notion.app.dto

import jakarta.validation.constraints.*
import java.io.Serializable

data class UserDto(
    @Min(1)
    @NotNull
    val userId: Int,

    @NotNull
    @NotBlank
    @Email
    @Size(max = 255)
    val email: String,

    @NotBlank
    @NotNull
    @Size(max = 255)
    val password: String
): Serializable