package org.example.notion.app.paragraph.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.example.notion.app.paragraph.entity.ParagraphType

data class ParagraphGetResponse(

    @Min(1)
    val id: Long,

    @Min(1)
    val noteId: Long,

    @Size(max = 255)
    val title: String,

    val nextParagraphId: Long?,

    val text: String,

    @NotBlank
    val paragraphType: ParagraphType,

    val imageUrls: List<String>
) {
    class Builder {
        private var id: Long = 0
        private var noteId: Long = 0
        private var title: String = ""
        private var nextParagraphId: Long? = 0
        private var text: String = ""
        private var paragraphType: ParagraphType? = null
        private var imageUrls: List<String> = emptyList()

        fun id(id: Long) = apply { this.id = id }
        fun noteId(noteId: Long) = apply { this.noteId = noteId }
        fun title(title: String) = apply { this.title = title }
        fun nextParagraphId(nextParagraphId: Long?) = apply { this.nextParagraphId = nextParagraphId }
        fun text(text: String) = apply { this.text = text }
        fun paragraphType(paragraphType: ParagraphType) = apply { this.paragraphType = paragraphType }
        fun imageUrls(imageUrls: List<String>) = apply { this.imageUrls = imageUrls }

        fun build(): ParagraphGetResponse {
            require(noteId > 0) { "Note ID must be greater than 0" }
            require(title.length <= 255) { "Title must not exceed 255 characters" }
            require(text.isNotBlank()) { "Text cannot be blank" }
            require(paragraphType != null) { "Paragraph type must not be null" }

            return ParagraphGetResponse(
                id = id,
                noteId = noteId,
                title = title,
                nextParagraphId = nextParagraphId,
                text = text,
                paragraphType = paragraphType!!,
                imageUrls = imageUrls
            )
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParagraphGetResponse) return false

        if (noteId != other.noteId) return false
        if (title != other.title) return false
        if (nextParagraphId != other.nextParagraphId) return false
        if (text != other.text) return false
        if (paragraphType != other.paragraphType) return false
        if (id != other.id) return false

        // Сравниваем imageUrls без query-параметров
        if (imageUrls.map { it.split("?")[0] } != other.imageUrls.map { it.split("?")[0] }) return false

        return true
    }

    override fun hashCode(): Int {
        var result = noteId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + nextParagraphId.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + paragraphType.hashCode()
        result = 31 * result + imageUrls.map { it.split("?")[0] }.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
