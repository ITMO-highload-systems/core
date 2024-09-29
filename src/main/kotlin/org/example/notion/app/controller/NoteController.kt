package org.example.notion.app.controller

import org.example.notion.app.dto.NoteDto
import org.example.notion.app.service.NoteService
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
    fun getNoteById(@PathVariable("noteId") noteId: Int): ResponseEntity<NoteDto> {
        return ResponseEntity.ok(noteService.getByNoteId(noteId))
    }

    @GetMapping("/owner/{owner}")
    fun getNoteByOwner(@PathVariable("owner") owner: Int): ResponseEntity<List<NoteDto>> {
        return ResponseEntity.ok(noteService.getByOwner(owner))
    }

    @DeleteMapping("{noteId}")
    fun deleteByNoteId(@PathVariable("noteId") noteId: Int): ResponseEntity<Unit> {
        noteService.deleteByNoteId(noteId)
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping()
    fun createNote(@Validated @RequestBody noteDto: NoteDto): ResponseEntity<Unit> {
        noteService.create(noteDto)
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping()
    fun updateNote(
        @Validated @RequestBody noteDto: NoteDto
    ): ResponseEntity<Int> {
        return ResponseEntity(noteService.update(noteDto), HttpStatus.OK)
    }

}