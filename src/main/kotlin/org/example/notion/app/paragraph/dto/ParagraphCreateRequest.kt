package org.example.notion.app.paragraph.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.example.notion.app.paragraph.entity.ParagraphType
import org.springframework.web.multipart.MultipartFile

data class ParagraphCreateRequest(
    @Min(1)
    val noteId: Long,

    @Size(max = 255)
    val title: String,

    val nextParagraphId: Long?,

    val text: String,

    @NotBlank
    val paragraphType: ParagraphType,

    val images: List<MultipartFile> = emptyList()
) {
    class Builder {
        private var noteId: Long = 0
        private var title: String = ""
        private var nextParagraphId: Long? = null
        private var text: String = ""
        private var paragraphType: ParagraphType? = null
        private var images: List<MultipartFile> = emptyList()

        fun noteId(noteId: Long) = apply { this.noteId = noteId }
        fun title(title: String) = apply { this.title = title }
        fun nextParagraphId(nextParagraphId: Long) = apply { this.nextParagraphId = nextParagraphId }
        fun text(text: String) = apply { this.text = text }
        fun paragraphType(paragraphType: ParagraphType) = apply { this.paragraphType = paragraphType }
        fun images(images: List<MultipartFile>) = apply { this.images = images }

        fun build(): ParagraphCreateRequest {
            require(noteId > 0) { "Note ID must be greater than 0" }
            require(title.length <= 255) { "Title must not exceed 255 characters" }
            require(text.isNotBlank()) { "Text cannot be blank" }
            require(paragraphType != null) { "Paragraph type must not be null" }

            return ParagraphCreateRequest(
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