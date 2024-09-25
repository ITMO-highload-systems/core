package org.example.notion.sse

data class Message(
    override val type: Type,
    val objectValue: String
) : AbstractMessage(type)