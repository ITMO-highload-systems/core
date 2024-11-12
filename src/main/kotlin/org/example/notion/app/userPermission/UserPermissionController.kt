package org.example.notion.app.userPermission

import org.example.notion.app.userPermission.dto.NoteUserPermissionDeleteDto
import org.example.notion.app.userPermission.dto.NoteUserPermissionDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/user/permissions")
class UserPermissionController(
    private val userPermissionService: UserPermissionService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @RequestBody noteUserPermission: NoteUserPermissionDto
    ): ResponseEntity<Void> {
        userPermissionService.create(noteUserPermission)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @DeleteMapping
    fun delete(
        @RequestBody noteUserPermission: NoteUserPermissionDeleteDto
    ): ResponseEntity<Void> {
        userPermissionService.delete(noteUserPermission)
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @RequestBody noteUserPermission: NoteUserPermissionDto
    ): ResponseEntity<Void> {
        userPermissionService.update(noteUserPermission)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/{noteId}")
    fun get(
        @PathVariable noteId: Long
    ): ResponseEntity<List<NoteUserPermissionDto>> {
        return ResponseEntity.ok(userPermissionService.findByNoteId(noteId))
    }

}