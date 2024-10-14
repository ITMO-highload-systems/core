package org.example.notion.app.team.service

import org.example.notion.app.exceptions.EntityAlreadyExistException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.exceptions.ForbiddenException
import org.example.notion.app.team.dto.TeamCreateDto
import org.example.notion.app.team.dto.TeamDto
import org.example.notion.app.team.mapper.TeamMapper
import org.example.notion.app.team.repository.TeamRepository
import org.example.notion.app.teamUser.TeamUserService
import org.example.notion.app.user.UserContext
import org.example.notion.app.user.UserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamMapper: TeamMapper,
    private val userService: UserService,
    @Lazy
    private val teamUserService: TeamUserService
) {
    fun getByTeamId(teamId: Long): TeamDto {
        teamUserService.requireParticipant(teamId)
        val team =
            teamRepository.findTeamByTeamId(teamId) ?: throw EntityNotFoundException("Team with id $teamId not found")

        return teamMapper.toDto(team)
    }

    fun requireTeamExistence(teamId: Long) {
        teamRepository.findTeamByTeamId(teamId) ?: throw
            EntityNotFoundException("Team with id $teamId not found")

    }

    fun getMyTeams(): List<TeamDto> {
        return getTeamsByUser(userService.getCurrentUser())
    }

    fun getTeamsByUser(userId: Long): List<TeamDto> {
        val teamList = teamRepository.findByOwner(userId)
        return teamList.map { teamMapper.toDto(it) }
    }

    @Transactional
    fun update(teamDto: TeamDto): TeamDto {
        userService.requireUserExistence(teamDto.owner)
        requireTeamExistence(teamDto.teamId)
        requireCurrentUserIsOwner(teamDto.teamId)
        val currentTeam = (teamRepository.findTeamByTeamId(teamDto.teamId)
            ?: throw EntityNotFoundException("Team with id ${teamDto.teamId} not found"))
        if (currentTeam.name != teamDto.name && teamRepository.findTeamByName(teamDto.name) != null) {
            throw EntityAlreadyExistException("Team with name ${teamDto.name} already exist")
        }
        return teamMapper.toDto(teamRepository.save(teamMapper.toEntity(teamDto)))
    }

    fun deleteByTeamId(teamId: Long) {
        requireCurrentUserIsOwner(teamId)
        teamRepository.deleteById(teamId)
    }

    @Transactional
    fun createTeam(teamDto: TeamCreateDto): TeamDto {
        if (teamRepository.findTeamByName(teamDto.name) != null)
            throw EntityAlreadyExistException("Team with name ${teamDto.name} already exists")
        val toEntity = teamMapper.toEntity(teamDto.name, userService.getCurrentUser())
        return teamMapper.toDto(teamRepository.save(toEntity))
    }

    fun isOwner(teamId: Long, userId: Long): Boolean {
        val result = teamRepository.findTeamByTeamId(teamId)
            ?: throw EntityNotFoundException("Team with id $teamId does not exist")
        return result.owner == userId
    }

    fun requireCurrentUserIsOwner(teamId: Long) {
        val result = teamRepository.findTeamByTeamId(teamId)
            ?: throw EntityNotFoundException("Team with id $teamId does not exist")
        if (result.owner != userService.getCurrentUser()) {
            throw ForbiddenException("You are not allowed to update this team")
        }
    }
}