package org.example.notion.app.team.mapper

import org.example.notion.app.team.dto.TeamDto
import org.example.notion.app.team.entity.Team
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface TeamMapper {
    fun toEntity(teamDto: TeamDto): Team

    @Mapping(target = "name", source = "name")
    @Mapping(target = "owner", source = "owner")
    fun toEntity(name: String, owner: Long): Team
    fun toDto(team: Team): TeamDto
}