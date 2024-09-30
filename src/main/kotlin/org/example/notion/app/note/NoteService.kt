package org.example.notion.app.note

import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.note.dto.NoteCreateDto
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.note.dto.NoteUpdateDto
import org.example.notion.app.note.mapper.NoteMapper
import org.example.notion.app.userPermission.UserPermissionService
import org.example.notion.app.userPermission.entity.Permission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val userPermissionService: UserPermissionService,
    private val noteMapper: NoteMapper
) {

    @Transactional
    fun create(noteDto: NoteCreateDto): NoteDto {
        val entity = noteMapper.toEntity(noteDto)

        val save = noteRepository.save(entity)
        return noteMapper.toDto(save)
    }

    @Transactional
    fun getByNoteId(noteId: Long): NoteDto {
        userPermissionService.requireUserPermission(noteId, Permission.READER)
        val note = noteRepository.findByNoteId(noteId)
            .orElseThrow { EntityNotFoundException("Note with id $noteId not found") }

        return noteMapper.toDto(note)
    }


    fun getByOwnerId(ownerId: Long): List<NoteDto> {
        return noteRepository.findByOwner(ownerId).let {
            it.map { el -> noteMapper.toDto(el) }
        }
    }

    @Transactional
    fun deleteByNoteId(noteId: Long) {
        userPermissionService.requireOwnerPermission(noteId)
        noteRepository.deleteByNoteId(noteId)
    }

    @Transactional
    fun update(noteDto: NoteUpdateDto): NoteDto {
        userPermissionService.requireOwnerPermission(noteDto.noteId)
        val note = noteRepository.findByNoteId(noteDto.noteId)
            .orElseThrow { EntityNotFoundException("Note with id ${noteDto.noteId} not found") }

        if (noteDto.owner != note.owner) {
            userPermissionService.requireOwnerPermission(noteDto.noteId)
        }

        val newEntity = noteMapper.toEntity(noteDto)
        val saved = noteRepository.save(newEntity)

        return noteMapper.toDto(saved)
    }

    fun isOwner(noteId: Long, userId: Long): Boolean {
        val result = noteRepository.findByNoteId(noteId)
        return result.isPresent && result.get().owner == userId
    }
}

