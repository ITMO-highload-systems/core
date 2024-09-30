package org.example.notion.app.paragraph.mapper

import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.entity.Paragraph
import org.mapstruct.Mapper

@Mapper
interface ParagraphMapper {

    fun toEntity(dto: ParagraphUpdateRequest): Paragraph

    fun toDto(entity: Paragraph): ParagraphUpdateRequest
}