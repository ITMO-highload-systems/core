package org.example.notion.app.team.mapper

import org.example.notion.app.team.dto.TeamCreateDto
import org.example.notion.app.team.dto.TeamDto
import org.example.notion.app.team.entity.Team
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface TeamMapper {
    fun toEntity(teamDto: TeamDto): Team
    fun toEntity(teamDto: TeamCreateDto): Team
    fun toDto(team: Team): TeamDto
}