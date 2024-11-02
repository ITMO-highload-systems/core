package org.example.notion.app.note

import org.example.notion.app.note.dto.NoteCreateDto
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.note.dto.NoteUpdateDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/note")
class NoteController(
    private val noteService: NoteService
) {

    @GetMapping("{noteId}")
    fun getNoteById(
        @PathVariable("noteId") noteId: Long
    ): ResponseEntity<NoteDto> {
        return ResponseEntity.ok(noteService.getByNoteId(noteId))
    }

    @GetMapping("/my")
    fun getNoteByOwnerId(): ResponseEntity<List<NoteDto>> {
        return ResponseEntity.ok(noteService.getByOwnerId())
    }

    @DeleteMapping("{noteId}")
    fun deleteByNoteId(
        @PathVariable("noteId") noteId: Long
    ): ResponseEntity<Void> {
        noteService.deleteByNoteId(noteId)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping
    fun createNote(
        @Validated @RequestBody noteCreateDto: NoteCreateDto
    ): ResponseEntity<NoteDto> {
        return ResponseEntity(noteService.create(noteCreateDto), HttpStatus.CREATED)
    }

    @PutMapping
    fun updateNote(
        @Validated @RequestBody noteUpdateDto: NoteUpdateDto
    ): ResponseEntity<NoteDto> {
        return ResponseEntity.ok(noteService.update(noteUpdateDto))
    }

}