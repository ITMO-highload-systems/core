package org.example.notion.app.paragraph.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ChangeParagraphPositionRequest(
    @Min(1)
    val paragraphId: Long,

    val nextParagraphId: Long?
)