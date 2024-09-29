package org.example.notion.app.advice

data class ApiException(
    val code: Int,
    val message: String?
)