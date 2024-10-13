package org.example.notion.app.userPermission

import org.example.notion.app.exceptions.EntityAlreadyExistException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.exceptions.ForbiddenException
import org.example.notion.app.note.NoteService
import org.example.notion.app.team.service.TeamService
import org.example.notion.app.teamUser.TeamUserService
import org.example.notion.app.user.UserService
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDeleteDto
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDto
import org.example.notion.app.userPermission.entity.NoteTeamPermission
import org.example.notion.app.userPermission.entity.Permission
import org.example.notion.app.userPermission.mapper.TeamPermissionMapper
import org.example.notion.permission.PermissionService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamPermissionService(
    private val teamPermissionRepository: TeamPermissionRepository,
    private val teamPermissionMapper: TeamPermissionMapper,
    private val teamService: TeamService,
    private val noteService: NoteService,
    private val teamUserService: TeamUserService,
    private val permissionService: PermissionService,
    @Lazy
    private val userService: UserService,
) {


    @Transactional
    fun create(noteTeamPermissionDto: NoteTeamPermissionDto): NoteTeamPermissionDto {
        permissionService.requireOwnerPermission(noteTeamPermissionDto.noteId)
        if (teamPermissionRepository.findNoteTeamPermissionByTeamIdAndNoteId(
                noteTeamPermissionDto.teamId,
                noteTeamPermissionDto.noteId
            ) != null
        ) {
            throw EntityAlreadyExistException("The permission for team ${noteTeamPermissionDto.teamId} and note ${noteTeamPermissionDto.noteId} already exists")
        }
        teamService.requireTeamExistence(noteTeamPermissionDto.teamId)
        return teamPermissionMapper.toDto(
            teamPermissionRepository.save(
                teamPermissionMapper.toEntity(
                    noteTeamPermissionDto
                )
            )
        )
    }

    @Transactional
    fun update(noteTeamPermissionDto: NoteTeamPermissionDto): NoteTeamPermissionDto {
        permissionService.requireOwnerPermission(noteTeamPermissionDto.noteId)
        val existing = teamPermissionRepository.findNoteTeamPermissionByTeamIdAndNoteId(
            noteTeamPermissionDto.teamId,
            noteTeamPermissionDto.noteId
        )
            ?: throw EntityNotFoundException("Can not find permission with teamId = ${noteTeamPermissionDto.teamId} and noteId = ${noteTeamPermissionDto.noteId}")

        return teamPermissionMapper.toDto(
            teamPermissionRepository.save(
                NoteTeamPermission(
                    permissionId = existing.permissionId,
                    noteId = noteTeamPermissionDto.noteId,
                    teamId = noteTeamPermissionDto.teamId,
                    permission = noteTeamPermissionDto.permission
                )
            )
        )
    }

    @Transactional
    fun delete(noteTeamPermissionDeleteDto: NoteTeamPermissionDeleteDto) {
        val currentUserId = userService.getCurrentUser()
        val isTeamOwner = teamService.isOwner(noteTeamPermissionDeleteDto.teamId, currentUserId)
        val isNoteOwner = noteService.isOwner(noteTeamPermissionDeleteDto.noteId, currentUserId)
        if (isTeamOwner || isNoteOwner) {
            teamPermissionRepository.deleteNoteUserPermissionByTeamIdAndNoteId(
                noteTeamPermissionDeleteDto.teamId,
                noteTeamPermissionDeleteDto.noteId
            )
        } else {
            throw ForbiddenException("Current user $currentUserId must be owner of note ${noteTeamPermissionDeleteDto.noteId} or team ${noteTeamPermissionDeleteDto.teamId}")
        }
    }

    @Transactional
    fun findByNoteId(noteId: Long): List<NoteTeamPermissionDto> {
        permissionService.requireUserPermission(noteId, Permission.READER)
        return teamPermissionRepository.findAllByNoteId(noteId).map { teamPermissionMapper.toDto(it) }
    }

    @Transactional
    fun findByNoteIdUnsafe(noteId: Long): List<NoteTeamPermissionDto> {
        return teamPermissionRepository.findAllByNoteId(noteId).map { teamPermissionMapper.toDto(it) }
    }

    @Transactional
    fun findByTeamId(teamId: Long): List<NoteTeamPermissionDto> {
        teamUserService.requireParticipant(teamId)
        return teamPermissionRepository.findAllByTeamId(teamId).map { teamPermissionMapper.toDto(it) }
    }

}

