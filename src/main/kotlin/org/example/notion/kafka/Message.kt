package org.example.notion.kafka

data class Message(
    override val type: Type,
    val message: String,
    val noteId: Long
) : AbstractMessage(type)