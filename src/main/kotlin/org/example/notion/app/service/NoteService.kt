package org.example.notion.app.service

import org.example.notion.app.dto.NoteDto
import org.example.notion.app.entity.Note
import org.example.notion.app.repository.NoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoteService(
    private val noteRepository: NoteRepository,
) {
    fun create(noteDto: NoteDto) =
        noteRepository.save(noteDto)

    //TODO или лучше если нет note возвращать exception?
    fun getByNoteId(noteId: Int): NoteDto? =
        noteRepository.findByNoteId(noteId)?.toDto()

    fun getByOwner(owner: Int): List<NoteDto> =
        noteRepository.findByOwner(owner).map { it.toDto() }

    fun deleteByNoteId(noteId: Int): Int =
        noteRepository.deleteByNoteId(noteId)

    //TODO делать ли тут проверку на сущ owner?
    fun deleteByOwner(owner: Int): Int =
        noteRepository.deleteByOwner(owner)

    //TODO Пока сделал общее обновление всей сущности, но можно подумать про динамическое обновление
    @Transactional
    fun update(noteId: Int, noteDto: NoteDto): Int {
        if (noteRepository.findByNoteId(noteId) == null) return 0
        return noteRepository.updateNote(noteId, noteDto)
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
