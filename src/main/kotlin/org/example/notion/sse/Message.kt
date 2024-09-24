package org.example.notion.sse

import com.fasterxml.jackson.annotation.JsonProperty

data class Message(
    val type: Type,
    @JsonProperty("object_value")
    val objectValue: Any?
)