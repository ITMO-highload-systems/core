package org.example.notion.app.paragraph.mapper

import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.entity.Paragraph
import org.mapstruct.Mapper

@Mapper
abstract class ParagraphMapper {

    abstract fun toEntity(dto: ParagraphUpdateRequest): Paragraph

    abstract fun toDto(entity: Paragraph): ParagraphUpdateRequest

    fun toEntity(dto: ParagraphCreateRequest, userId: Long): Paragraph {
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