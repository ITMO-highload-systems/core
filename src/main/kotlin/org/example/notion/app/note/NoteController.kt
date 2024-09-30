package org.example.notion.app.note

import org.example.notion.app.user.UserContext
import org.example.notion.app.note.dto.NoteCreateDto
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.note.dto.NoteUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/note")
class NoteController(
    private val noteService: NoteService
) {

    @GetMapping("{noteId}")
    fun getNoteById(
        @PathVariable("noteId") noteId: Long,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<NoteDto> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(noteService.getByNoteId(noteId))
    }

    @GetMapping("/owner/{owner}")
    fun getNoteByOwnerId(
        @PathVariable("owner") ownerId: Long,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<List<NoteDto>> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(noteService.getByOwnerId(ownerId))
    }

    @DeleteMapping("{noteId}")
    fun deleteByNoteId(
        @PathVariable("noteId") noteId: Long,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<Void> {
        UserContext.setCurrentUser(userId)
        noteService.deleteByNoteId(noteId)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping
    fun createNote(
        @Validated @RequestBody noteCreateDto: NoteCreateDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<NoteDto> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(noteService.create(noteCreateDto))
    }

    @PutMapping
    fun updateNote(
        @Validated @RequestBody noteUpdateDto: NoteUpdateDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<NoteDto> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(noteService.update(noteUpdateDto))
    }

}