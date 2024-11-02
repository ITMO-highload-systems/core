package org.example.notion.app.paragraph.mapper

import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.entity.Paragraph
import org.springframework.stereotype.Component

@Component
class ParagraphMapper {

    fun toEntity(dto: ParagraphCreateRequest, userId: String): Paragraph {
        return Paragraph.Builder()
            .noteId(dto.noteId)
            .title(dto.title)
            .text(dto.text)
            .nextParagraphId(dto.nextParagraphId)
            .paragraphType(dto.paragraphType)
            .lastUpdateUserId(userId)
            .build()
    }
}