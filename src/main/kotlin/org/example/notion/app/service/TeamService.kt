package org.example.notion.app.service

import org.example.notion.app.dto.TeamDto
import org.example.notion.app.entity.Team
import org.example.notion.app.exceptions.BadEntityRequestException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.repository.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamService(
    val teamRepository: TeamRepository
) {
    fun getByTeamId(teamId: Int): TeamDto {
        return teamRepository.findByTeamId(teamId).let {
            if (it == null) throw EntityNotFoundException("Team with id $teamId not found")
            it.toDto()
        }
    }

    fun getByName(name: String): TeamDto {
        return teamRepository.findByName(name).let {
            if (it == null) throw EntityNotFoundException("Team with name $name not found")
            it.toDto()
        }
    }

    @Transactional
    fun update(teamDto: TeamDto): Int {
        if (teamRepository.findByTeamId(teamDto.teamId) == null)
            throw EntityNotFoundException("Team with id ${teamDto.teamId} not found")
        if (teamRepository.findByName(teamDto.name) != null)
            throw BadEntityRequestException("Team with name ${teamDto.name} already exists")
        return teamRepository.update(teamDto)
    }

    fun deleteByTeamId(teamId: Int): Int {
        return teamRepository.deleteByTeamId(teamId)
    }

    @Transactional
    fun createTeam(teamDto: TeamDto): Int {
        if (teamRepository.findByName(teamDto.name) != null)
            throw BadEntityRequestException("Team with name ${teamDto.name} already exists")
        return teamRepository.save(teamDto)
    }

    private fun Team.toDto(): TeamDto =
        TeamDto(
            teamId = this.teamId,
            name = this.name,
            owner = this.owner,
        )
}