package org.example.notion.app.paragraph.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.example.notion.app.paragraph.entity.ParagraphType
import java.io.InputStream

data class ParagraphGetResponse(
    @NotNull @Min(1)
    val noteId: Long,

    @Size(max = 255)
    val title: String,

    @NotNull
    val nextParagraphId: Long,

    @NotNull
    val text: String,

    @NotBlank
    val paragraphType: ParagraphType,

    val images: List<InputStream>
) {
    class Builder {
        private var noteId: Long = 0
        private var title: String = ""
        private var nextParagraphId: Long = 0
        private var text: String = ""
        private var paragraphType: ParagraphType? = null
        private var images: List<InputStream> = emptyList()

        fun noteId(noteId: Long) = apply { this.noteId = noteId }
        fun title(title: String) = apply { this.title = title }
        fun nextParagraphId(nextParagraphId: Long) = apply { this.nextParagraphId = nextParagraphId }
        fun text(text: String) = apply { this.text = text }
        fun paragraphType(paragraphType: ParagraphType) = apply { this.paragraphType = paragraphType }
        fun images(images: List<InputStream>) = apply { this.images = images }

        fun build(): ParagraphGetResponse {
            require(noteId > 0) { "Note ID must be greater than 0" }
            require(title.length <= 255) { "Title must not exceed 255 characters" }
            require(nextParagraphId >= 0) { "nextParagraphId must be non-negative" }
            require(text.isNotBlank()) { "Text cannot be blank" }
            require(paragraphType != null) { "Paragraph type must not be null" }

            return ParagraphGetResponse(
                noteId = noteId,
                title = title,
                nextParagraphId = nextParagraphId,
                text = text,
                paragraphType = paragraphType!!,
                images = images
            )
        }
    }
}
