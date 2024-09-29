package org.example.notion.app.user.dto

import jakarta.validation.constraints.*
import java.io.Serializable

data class UserResponseDto(
    val userId: Long,

    @NotBlank
    @Email
    @Size(max = 255)
    val email: String,

    ): Serializable