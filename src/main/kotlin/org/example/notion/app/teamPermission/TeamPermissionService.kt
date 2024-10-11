package org.example.notion.app.userPermission

import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.exceptions.ForbiddenException
import org.example.notion.app.note.NoteService
import org.example.notion.app.team.service.TeamService
import org.example.notion.app.user.UserService
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDeleteDto
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDto
import org.example.notion.app.userPermission.entity.NoteTeamPermission
import org.example.notion.app.userPermission.entity.Permission
import org.example.notion.app.userPermission.mapper.TeamPermissionMapper
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class TeamPermissionService(
    private val teamPermissionRepository: TeamPermissionRepository,
    private val teamPermissionMapper: TeamPermissionMapper,
    @Lazy
    private val userService: UserService,
    private val teamService: TeamService,
    private val noteService: NoteService,
    private val userPermissionRepository: UserPermissionRepository
) {

    @Transactional
    fun create(noteTeamPermissionDto: NoteTeamPermissionDto) {
        requireOwnerPermission(noteTeamPermissionDto.noteId)
        teamPermissionRepository.save(teamPermissionMapper.toEntity(noteTeamPermissionDto))
    }

    @Transactional
    fun update(noteTeamPermissionDto: NoteTeamPermissionDto) {
        requireOwnerPermission(noteTeamPermissionDto.noteId)
        val existing = teamPermissionRepository.findNoteUserPermissionByTeamIdAndNoteId(
            noteTeamPermissionDto.teamId,
            noteTeamPermissionDto.noteId
        ).getOrElse {
            throw EntityNotFoundException("Can not find entity with teamId = ${noteTeamPermissionDto.teamId} and noteId = ${noteTeamPermissionDto.noteId}")
        }
        teamPermissionRepository.save(
            NoteTeamPermission(
                permissionId = existing.permissionId,
                noteId = noteTeamPermissionDto.noteId,
                teamId = noteTeamPermissionDto.teamId,
                permission = noteTeamPermissionDto.permission
            )
        )
    }

    @Transactional
    fun delete(noteTeamPermissionDeleteDto: NoteTeamPermissionDeleteDto) {
        requireOwnerPermission(noteTeamPermissionDeleteDto.noteId)
        teamPermissionRepository.deleteNoteUserPermissionByTeamIdAndNoteId(
            noteTeamPermissionDeleteDto.teamId,
            noteTeamPermissionDeleteDto.noteId
        )
    }

    @Transactional
    fun findByTeamIdAndNoteId(noteId: Long, teamId: Long): Permission {
        requireUserPermission(noteId, Permission.READER)
        return teamPermissionRepository.findNoteUserPermissionByTeamIdAndNoteId(noteId, teamId).orElseThrow {
            EntityNotFoundException("Permission for team $teamId and note $noteId not found")
        }.permission
    }

    @Transactional
    fun findByNoteId(noteId: Long): List<NoteTeamPermissionDto> {
        requireUserPermission(noteId, Permission.READER)
        return teamPermissionRepository.findAllByNoteId(noteId).map { teamPermissionMapper.toDto(it) }
    }

    @Transactional
    fun requireUserPermission(noteId: Long, permission: Permission) {
        //todo не учитывает команду
        val userId = userService.getCurrentUser()
        if (noteService.isOwner(noteId, userId)) {
            return
        }
        val myPermission =
            userPermissionRepository.findNoteUserPermissionByUserIdAndNoteId(userId, noteId).orElseThrow {
                throw ForbiddenException("Permission for user $userId and note $noteId not found")
            }.permission

        if (myPermission < permission) {
            throw ForbiddenException("Forbidden permission for user $userId need $permission")
        }
    }

    @Transactional
    fun requireOwnerPermission(noteId: Long) {
        val userId = userService.getCurrentUser()
        if (!noteService.isOwner(noteId, userId))
            throw ForbiddenException("Forbidden permission for user $userId must be owner of $noteId")

    }
}

