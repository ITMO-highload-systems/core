package org.example.notion.sse

import com.fasterxml.jackson.annotation.JsonProperty

@JvmRecord
data class Message(
    val type: Type,
    @JsonProperty("object_value")
    val objectValue: Any?
)