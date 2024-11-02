package org.example.notion.app.teamUser

import org.example.notion.app.exceptions.EntityAlreadyExistException
import org.example.notion.app.exceptions.ForbiddenException
import org.example.notion.app.team.service.TeamService
import org.example.notion.app.teamUser.dto.TeamUserCreateDto
import org.example.notion.app.teamUser.dto.TeamUserMyResponseDto
import org.example.notion.app.teamUser.dto.TeamUserResponseDto
import org.example.notion.app.teamUser.mapper.TeamUserMapper
import org.example.notion.app.user.UserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamUserService(
    @Lazy
    private val userService: UserService,
    private val teamService: TeamService,
    private val teamUserRepository: TeamUserRepository,
    private val teamUserMapper: TeamUserMapper
) {

    @Transactional
    fun requireParticipant(teamId: Long) {
        val userId = userService.getCurrentUser()
        if (teamService.isOwner(teamId, userId)) {
            return
        }
        teamUserRepository.findTeamUserByTeamIdAndUserId(teamId, userId)
            ?: throw ForbiddenException("User $userId not in team $teamId")
    }

    @Transactional
    fun create(teamUserDto: TeamUserCreateDto): TeamUserResponseDto {
        userService.requireUserExistence(teamUserDto.userId)
        teamService.requireTeamExistence(teamUserDto.teamId)
        teamService.requireCurrentUserIsOwner(teamUserDto.teamId)
        if (teamUserRepository.findTeamUserByTeamIdAndUserId(teamUserDto.teamId, teamUserDto.userId) != null) {
            throw EntityAlreadyExistException("User ${teamUserDto.userId} in team ${teamUserDto.teamId} already exists")
        }
        val saved = teamUserRepository.save(teamUserMapper.toEntity(teamUserDto))
        return teamUserMapper.toResponseDto(saved)
    }

    @Transactional
    fun delete(teamUserDto: TeamUserCreateDto) {
        teamService.requireCurrentUserIsOwner(teamUserDto.teamId)
        teamUserRepository.deleteTeamUserByTeamIdAndUserId(teamUserDto.teamId, teamUserDto.userId)
    }

    @Transactional(readOnly = true)
    fun findByTeamId(teamId: Long): List<TeamUserResponseDto> {
        teamService.requireTeamExistence(teamId)
        requireParticipant(teamId)
        return teamUserRepository.findAllByTeamId(teamId).map { teamUser -> teamUserMapper.toResponseDto(teamUser) }
    }

    @Transactional(readOnly = true)
    fun findByUserId(): List<TeamUserMyResponseDto> {
        return findByUserId(userService.getCurrentUser())

    }

    @Transactional(readOnly = true)
    fun findByUserId(userId: String): List<TeamUserMyResponseDto> {
        return teamUserRepository.findAllByUserId(userId)
            .map { teamUser -> teamUserMapper.toMyResponseDto(teamUser) }

    }
}

