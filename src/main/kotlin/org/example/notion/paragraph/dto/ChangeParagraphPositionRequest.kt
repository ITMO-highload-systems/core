package org.example.notion.paragraph.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ChangeParagraphPositionRequest(
    @Min(1)
    @NotNull
    val paragraphId: Long,

    @Min(2)
    @NotNull
    val nextParagraphId: Long
)