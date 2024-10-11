package org.example.notion.app.userPermission

import org.example.notion.app.user.UserContext
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDeleteDto
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/team/permissions")
class TeamPermissionController(
    private val teamPermissionService: TeamPermissionService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @RequestBody teamPermissionDto: NoteTeamPermissionDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<Void> {
        UserContext.setCurrentUser(userId)
        teamPermissionService.create(teamPermissionDto)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @DeleteMapping
    fun delete(
        @RequestBody noteTeamPermissionDeleteDto: NoteTeamPermissionDeleteDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<Void> {
        UserContext.setCurrentUser(userId)
        teamPermissionService.delete(noteTeamPermissionDeleteDto)
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping
    fun update(
        @RequestBody noteTeamPermissionDto: NoteTeamPermissionDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<Void> {
        UserContext.setCurrentUser(userId)
        teamPermissionService.update(noteTeamPermissionDto)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/{teamId}")
    fun get(
        @PathVariable teamId: Long,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<List<NoteTeamPermissionDto>> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(teamPermissionService.findByNoteId(teamId))
    }

}