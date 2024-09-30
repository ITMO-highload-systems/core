package org.example.notion.app.paragraph.entity

import java.time.LocalDateTime

data class Paragraph(
    val id: Long,
    val noteId: Long,
    var title: String,
    var nextParagraphId: Long,
    var text: String,
    var lastUpdateUserId: Int,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    var paragraphType: ParagraphType
) {
    data class Builder(
        var id: Long = 0,
        var noteId: Long = 0,
        var title: String = "",
        var nextParagraphId: Long = 0,
        var text: String = "",
        var lastUpdateUserId: Int = 0,
        var createdAt: LocalDateTime = LocalDateTime.now(),
        var updatedAt: LocalDateTime = LocalDateTime.now(),
        var paragraphType: ParagraphType = ParagraphType.PLAIN_TEXT_PARAGRAPH
    ) {
        fun id(id: Long) = apply { this.id = id }
        fun noteId(noteId: Long) = apply { this.noteId = noteId }
        fun title(title: String) = apply { this.title = title }
        fun nextParagraphId(nextParagraphId: Long) = apply { this.nextParagraphId = nextParagraphId }
        fun text(text: String) = apply { this.text = text }
        fun lastUpdateUserId(lastUpdateUserId: Int) = apply { this.lastUpdateUserId = lastUpdateUserId }
        fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
        fun updatedAt(updatedAt: LocalDateTime) = apply { this.updatedAt = updatedAt }
        fun paragraphType(paragraphType: ParagraphType) = apply { this.paragraphType = paragraphType }

        fun build() = Paragraph(
            id = id,
            noteId = noteId,
            title = title,
            nextParagraphId = nextParagraphId,
            text = text,
            lastUpdateUserId = lastUpdateUserId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            paragraphType = paragraphType
        )
    }
}