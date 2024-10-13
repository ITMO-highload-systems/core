package org.example.notion.app.teamUser.mapper

import org.example.notion.app.teamUser.dto.TeamUserCreateDto
import org.example.notion.app.teamUser.dto.TeamUserMyResponseDto
import org.example.notion.app.teamUser.dto.TeamUserResponseDto
import org.example.notion.app.teamUser.entity.TeamUser
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface TeamUserMapper {
    fun toEntity(source: TeamUserCreateDto): TeamUser
    fun toDto(source: TeamUser): TeamUserCreateDto
    fun toResponseDto(source: TeamUser): TeamUserResponseDto
    fun toMyResponseDto(source: TeamUser): TeamUserMyResponseDto
}