package org.example.notion.app.service

import org.example.notion.app.dto.NoteDto
import org.example.notion.app.entity.Note
import org.example.notion.app.exceptions.BadEntityRequestException
import org.example.notion.app.exceptions.EntityNotFoundException
import org.example.notion.app.repository.NoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoteService(
    private val noteRepository: NoteRepository,
) {

    fun create(noteDto: NoteDto) =
        noteRepository.save(noteDto)

    fun getByNoteId(noteId: Int): NoteDto {
        return noteRepository.findByNoteId(noteId).let {
            if (it == null) throw EntityNotFoundException("Note with id $noteId not found")
            it.toDto()
        }
    }


    fun getByOwner(owner: Int): List<NoteDto> {
        return noteRepository.findByOwner(owner).let {
            if (it.isEmpty()) throw EntityNotFoundException("Notes with owner $owner not found")
            it.map { el -> el.toDto() }
        }
    }

    fun deleteByNoteId(noteId: Int): Int =
        noteRepository.deleteByNoteId(noteId)

    @Transactional
    fun update(noteDto: NoteDto): Int {
        val note = noteRepository.findByNoteId(noteDto.noteId)
            ?: throw EntityNotFoundException("Note with id ${noteDto.noteId} not found")

        if (noteDto.owner != note.owner) throw BadEntityRequestException("Bad request")

        return noteRepository.updateNote(noteDto)
    }

    private fun Note.toDto(): NoteDto =
        NoteDto(
            noteId = this.noteId,
            title = this.title,
            description = this.description,
            owner = this.owner,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
}

