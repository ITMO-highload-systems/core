package org.example.notion.app.paragraph.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Min

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChangeParagraphPositionRequest(
    @field:Min(1)
    val paragraphId: Long,

    val nextParagraphId: Long?
)