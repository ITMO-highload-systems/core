package org.example.notion.app.team.service

import org.example.notion.app.exceptions.EntityAlreadyExistException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.team.dto.TeamCreateDto
import org.example.notion.app.team.dto.TeamDto
import org.example.notion.app.team.mapper.TeamMapper
import org.example.notion.app.team.repository.TeamRepository
import org.example.notion.app.user.UserContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamService(
    val teamRepository: TeamRepository,
    val teamMapper: TeamMapper
) {
    fun getByTeamId(teamId: Long): TeamDto {
        val team = teamRepository.findTeamByTeamId(teamId).orElseThrow {
            EntityNotFoundException("Team with id $teamId not found")
        }
        return teamMapper.toDto(team)
    }
    fun requireTeamExistence(teamId: Long) {
        teamRepository.findTeamByTeamId(teamId).orElseThrow {
            EntityNotFoundException("Team with id $teamId not found")
        }
    }

    fun getMyTeams(): List<TeamDto> {
        val teamList = UserContext.getCurrentUser().let { teamRepository.findByOwner(it) }
        return teamList.map { teamMapper.toDto(it) }
    }

    fun getByName(name: String): TeamDto {
        val team = teamRepository.findTeamByName(name).orElseThrow {
            EntityNotFoundException("Team with name $name not found")
        }
        return teamMapper.toDto(team)
    }

    @Transactional
    fun update(teamDto: TeamDto): TeamDto {
        if (teamRepository.findTeamByTeamId(teamDto.teamId).isEmpty)
            throw EntityNotFoundException("Team with id ${teamDto.teamId} not found")
        if (teamRepository.findTeamByName(teamDto.name).isPresent)
            throw EntityAlreadyExistException("Team with name ${teamDto.name} already exists")
        return teamMapper.toDto(teamRepository.save(teamMapper.toEntity(teamDto)))
    }

    fun deleteByTeamId(teamId: Long) {
        teamRepository.deleteById(teamId)
    }

    @Transactional
    fun createTeam(teamDto: TeamCreateDto): TeamDto {
        if (teamRepository.findTeamByName(teamDto.name).isPresent)
            throw EntityAlreadyExistException("Team with name ${teamDto.name} already exists")
        return teamMapper.toDto(teamRepository.save(teamMapper.toEntity(teamDto)))
    }

    fun isOwner(teamId: Long, userId: Long): Boolean {
        val result = teamRepository.findTeamByTeamId(teamId)
        if (result.isEmpty) {
            throw EntityNotFoundException("Team with id $teamId does not exist")
        }
        return result.get().owner == userId
    }
}