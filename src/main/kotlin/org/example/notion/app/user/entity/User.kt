package org.example.notion.app.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("user")
data class User(
    @Id
    val userId: Int,
    val email: String,
    val password: String
)