package org.example.notion.sse

data class Message(
    val type: Type,
    val objectValue: Any?
)