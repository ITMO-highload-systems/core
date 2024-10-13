package org.example.notion.app.teamUser

import org.example.notion.app.teamUser.dto.TeamUserCreateDto
import org.example.notion.app.teamUser.dto.TeamUserMyResponseDto
import org.example.notion.app.teamUser.dto.TeamUserResponseDto
import org.example.notion.app.user.UserContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/team/user")
class TeamUserController(
    private val teamUserService: TeamUserService
) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @RequestBody teamUserDto: TeamUserCreateDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<TeamUserResponseDto> {
        UserContext.setCurrentUser(userId)
        val created = teamUserService.create(teamUserDto)
        return ResponseEntity<TeamUserResponseDto>(created, HttpStatus.CREATED)
    }

    @DeleteMapping
    fun delete(
        @RequestBody teamUserDto: TeamUserCreateDto,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<Void> {
        UserContext.setCurrentUser(userId)
        teamUserService.delete(teamUserDto)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/{teamId}")
    fun get(
        @PathVariable teamId: Long,
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<List<TeamUserResponseDto>> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(teamUserService.findByTeamId(teamId))
    }

    @GetMapping("/my")
    fun get(
        @RequestHeader("user-id") userId: Long
    ): ResponseEntity<List<TeamUserMyResponseDto>> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(teamUserService.findByUserId())
    }

}