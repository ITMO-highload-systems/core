package org.example.notion.paragraph.mapper

import org.example.notion.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.paragraph.entity.Paragraph
import org.mapstruct.Mapper

@Mapper
interface ParagraphMapper {

    fun toEntity(dto: ParagraphUpdateRequest): Paragraph

    fun toDto(entity: Paragraph): ParagraphUpdateRequest
}