package org.example.notion.app.userPermission.mapper

import org.example.notion.app.userPermission.dto.NoteTeamPermissionDto
import org.example.notion.app.userPermission.entity.NoteTeamPermission
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface TeamPermissionMapper {
    fun toEntity(source: NoteTeamPermissionDto): NoteTeamPermission
    fun toDto(source: NoteTeamPermission): NoteTeamPermissionDto
}