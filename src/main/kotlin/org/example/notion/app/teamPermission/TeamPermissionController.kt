package org.example.notion.app.userPermission

import org.example.notion.app.userPermission.dto.NoteTeamPermissionDeleteDto
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/team/permissions")
class TeamPermissionController(
    private val teamPermissionService: TeamPermissionService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @RequestBody teamPermissionDto: NoteTeamPermissionDto
    ): ResponseEntity<NoteTeamPermissionDto> {
        val created = teamPermissionService.create(teamPermissionDto)
        return ResponseEntity(created, HttpStatus.CREATED)
    }

    @DeleteMapping
    fun delete(
        @RequestBody noteTeamPermissionDeleteDto: NoteTeamPermissionDeleteDto
    ): ResponseEntity<Void> {
        teamPermissionService.delete(noteTeamPermissionDeleteDto)
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping
    fun update(
        @RequestBody noteTeamPermissionDto: NoteTeamPermissionDto
    ): ResponseEntity<NoteTeamPermissionDto> {
        val response = teamPermissionService.update(noteTeamPermissionDto)
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("/byTeam/{teamId}")
    fun get(
        @PathVariable teamId: Long
    ): ResponseEntity<List<NoteTeamPermissionDto>> {
        return ResponseEntity.ok(teamPermissionService.findByTeamId(teamId))
    }

    @GetMapping("byNote/{noteId}")
    fun getByNote(
        @PathVariable noteId: Long
    ): ResponseEntity<List<NoteTeamPermissionDto>> {
        return ResponseEntity.ok(teamPermissionService.findByNoteId(noteId))
    }

}