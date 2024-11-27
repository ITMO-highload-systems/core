package org.example.notion.app.paragraph.dto

data class ExecuteParagraphRequest(
    val paragraphid: Long,
    val code: String
)
