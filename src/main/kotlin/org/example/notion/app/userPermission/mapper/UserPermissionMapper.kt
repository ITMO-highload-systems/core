package org.example.notion.app.userPermission.mapper

import org.example.notion.app.userPermission.dto.NoteUserPermissionDto
import org.example.notion.app.userPermission.entity.NoteUserPermission
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface UserPermissionMapper {
    fun toEntity(source: NoteUserPermissionDto): NoteUserPermission
    fun toDto(source: NoteUserPermission): NoteUserPermissionDto
}