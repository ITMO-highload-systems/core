package org.example.notion.app.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable

data class UserCreateDto(

    @NotBlank
    @Email
    @Size(max = 255)
    val email: String,

    @NotBlank
    @Size(max = 255)
    val password: String
): Serializable