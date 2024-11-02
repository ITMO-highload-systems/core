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
import org.example.notion.permission.PermissionService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserPermissionService(
    private val userPermissionRepository: UserPermissionRepository,
    private val userService: UserService,
    private val userPermissionMapper: UserPermissionMapper,
    @Lazy
    private val noteService: NoteService,
    @Lazy
    private val permissionService: PermissionService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserPermissionRepository::class.java)
    }

    @Transactional
    fun create(userPermissionDto: NoteUserPermissionDto) {
        if (userPermissionDto.userId == userService.getCurrentUser()) {
            throw ForbiddenException("Can not change owner permissions")
        }
        permissionService.requireOwnerPermission(userPermissionDto.noteId)
        userService.requireUserExistence(userPermissionDto.userId)
        userPermissionRepository.save(userPermissionMapper.toEntity(userPermissionDto))
    }

    @Transactional
    fun update(userPermissionDto: NoteUserPermissionDto) {
        permissionService.requireOwnerPermission(userPermissionDto.noteId)
        val existing = userPermissionRepository.findNoteUserPermissionByUserIdAndNoteId(
            userPermissionDto.userId,
            userPermissionDto.noteId
        )
            ?: throw EntityNotFoundException("Can not find entity with userId = ${userPermissionDto.userId} and noteId = ${userPermissionDto.noteId}")

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
        permissionService.requireOwnerPermission(noteUserPermission.noteId)
        userPermissionRepository.deleteNoteUserPermissionByUserIdAndNoteId(
            noteUserPermission.userId,
            noteUserPermission.noteId
        )
    }

    @Transactional
    fun findByUserIdAndNoteId(noteId: Long, userId: String): Permission {
        permissionService.requireUserPermission(noteId, Permission.READER)
        val noteUserPermission = userPermissionRepository.findNoteUserPermissionByUserIdAndNoteId(userId, noteId)
            ?: throw EntityNotFoundException("Permission for user $userId and note $noteId not found")
        return noteUserPermission.permission

    }

    @Transactional
    fun findByNoteId(noteId: Long): List<NoteUserPermissionDto> {
        permissionService.requireUserPermission(noteId, Permission.READER)
        return userPermissionRepository.findAllByNoteId(noteId).map { userPermissionMapper.toDto(it) }
    }

    @Transactional
    fun isHasUserPermission(userId: String, noteId: Long, permission: Permission): Boolean {
        if (noteService.isOwner(noteId, userId)) {
            return true
        }
        val myPermission =
            userPermissionRepository.findNoteUserPermissionByUserIdAndNoteId(userId, noteId)
        if (myPermission == null) {
            logger.debug("Permission for user $userId and note $noteId not found")
            return false
        }

        if (myPermission.permission < permission) {
            logger.debug("Forbidden permission for user {} need {} has {}", userId, permission, myPermission.permission)
            return false
        }
        return true
    }

    fun deleteByNoteId(noteId: Long) {
        permissionService.requireOwnerPermission(noteId)
        userPermissionRepository.deleteNoteUserPermissionByNoteId(
            noteId
        )
    }

}

