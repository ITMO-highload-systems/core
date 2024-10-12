package org.example.notion.app.userPermission

import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.exceptions.ForbiddenException
import org.example.notion.app.note.NoteService
import org.example.notion.app.user.UserService
import org.example.notion.app.userPermission.dto.NoteUserPermissionDeleteDto
import org.example.notion.app.userPermission.dto.NoteUserPermissionDto
import org.example.notion.app.userPermission.entity.NoteUserPermission
import org.example.notion.app.userPermission.entity.Permission
import org.example.notion.app.userPermission.mapper.UserPermissionMapper
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
class UserPermissionService(
    private val userPermissionRepository: UserPermissionRepository,
    private val userService: UserService,
    private val userPermissionMapper: UserPermissionMapper,
    @Lazy
    private val noteService: NoteService
) {

    @Transactional
    fun create(userPermissionDto: NoteUserPermissionDto) {
        if (userPermissionDto.userId == userService.getCurrentUser()) {
            throw ForbiddenException("Can not change owner permissions")
        }
        requireOwnerPermission(userPermissionDto.noteId)
        userService.requireUserExistence(userPermissionDto.userId)
        userPermissionRepository.save(userPermissionMapper.toEntity(userPermissionDto))
    }

    @Transactional
    fun update(userPermissionDto: NoteUserPermissionDto) {
        requireOwnerPermission(userPermissionDto.noteId)
        val existing = userPermissionRepository.findNoteUserPermissionByUserIdAndNoteId(
            userPermissionDto.userId,
            userPermissionDto.noteId
        ).getOrElse {
            throw EntityNotFoundException("Can not find entity with userId = ${userPermissionDto.userId} and noteId = ${userPermissionDto.noteId}")
        }
        userPermissionRepository.save(
            NoteUserPermission(
                permissionId = existing.permissionId,
                noteId = userPermissionDto.noteId,
                userId = userPermissionDto.userId,
                permission = userPermissionDto.permission

            )
        )
    }

    @Transactional
    fun delete(noteUserPermission: NoteUserPermissionDeleteDto) {
        requireOwnerPermission(noteUserPermission.noteId)
        userPermissionRepository.deleteNoteUserPermissionByUserIdAndNoteId(
            noteUserPermission.userId,
            noteUserPermission.noteId
        )
    }

    @Transactional
    fun findByUserIdAndNoteId(noteId: Long, userId: Long): Permission {
        requireUserPermission(noteId, Permission.READER)
        return userPermissionRepository.findNoteUserPermissionByUserIdAndNoteId(noteId, userId).orElseThrow {
            EntityNotFoundException("Permission for user $userId and note $noteId not found")
        }.permission
    }

    @Transactional
    fun findByNoteId(noteId: Long): List<NoteUserPermissionDto> {
        requireUserPermission(noteId, Permission.READER)
        return userPermissionRepository.findAllByNoteId(noteId).map { userPermissionMapper.toDto(it) }
    }

    @Transactional
    fun requireUserPermission(noteId: Long, permission: Permission) {
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

