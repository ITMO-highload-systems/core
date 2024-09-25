package org.example.notion.app.controller

import org.example.notion.app.dto.NoteDto
import org.example.notion.app.service.NoteService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/note")
class NoteController(
    private val noteService: NoteService
) {

    @GetMapping("{noteId}")
    fun getNoteById(@PathVariable("noteId") noteId: Int): ResponseEntity<NoteDto?> {
        return ResponseEntity(noteService.getByNoteId(noteId), HttpStatus.OK)
    }

    @GetMapping("/owner/{owner}")
    fun getNoteByOwner(@PathVariable("owner") owner: Int): ResponseEntity<List<NoteDto>> {
        return ResponseEntity(noteService.getByOwner(owner), HttpStatus.OK)
    }

    @DeleteMapping("{noteId}")
    fun deleteByNoteId(@PathVariable("noteId") noteId: Int): ResponseEntity<Int> {
        return ResponseEntity(noteService.deleteByNoteId(noteId), HttpStatus.OK)
    }

    @DeleteMapping("/owner/{owner}")
    fun deleteByOwner(@PathVariable("owner") owner: Int): ResponseEntity<Int> {
        return ResponseEntity(noteService.deleteByOwner(owner), HttpStatus.OK)
    }

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createNote(@Validated @RequestBody noteDto: NoteDto): ResponseEntity<Unit> {
        noteService.create(noteDto)
        return ResponseEntity(HttpStatus.OK)
    }

    //TODO будем отправлять всю сущность для обновления или нет?
    @PutMapping("{noteId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateNote(
        @PathVariable("noteId") noteId: Int,
        @Validated @RequestBody noteDto: NoteDto
    ): ResponseEntity<Int> {
        return ResponseEntity(noteService.update(noteId, noteDto), HttpStatus.OK)
    }

}