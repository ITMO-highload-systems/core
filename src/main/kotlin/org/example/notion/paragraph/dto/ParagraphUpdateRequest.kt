package org.example.notion.paragraph.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.example.notion.paragraph.entity.ParagraphType
import org.springframework.web.multipart.MultipartFile

data class ParagraphUpdateRequest(
    @Min(1)
    val id: Long,

    @Size(max = 255)
    val title: String,

    @NotNull
    val text: String,

    @NotBlank
    val paragraphType: ParagraphType,

    val images: List<MultipartFile>
) {
    class Builder {
        private var id: Long = 0
        private var title: String = ""
        private var text: String = ""
        private var paragraphType: ParagraphType? = null
        private var images: List<MultipartFile> = emptyList()

        fun id(id: Long) = apply { this.id = id }
        fun title(title: String) = apply { this.title = title }
        fun text(text: String) = apply { this.text = text }
        fun paragraphType(paragraphType: ParagraphType) = apply { this.paragraphType = paragraphType }
        fun images(images: List<MultipartFile>) = apply { this.images = images }

        fun build(): ParagraphUpdateRequest {
            require(id > 0) { "ID must be greater than 0" }
            require(title.length <= 255) { "Title must not exceed 255 characters" }
            require(text.isNotBlank()) { "Text cannot be blank" }
            require(paragraphType != null) { "Paragraph type must not be null" }

            return ParagraphUpdateRequest(
                id = id,
                title = title,
                text = text,
                paragraphType = paragraphType!!,
                images = images
            )
        }
    }
}