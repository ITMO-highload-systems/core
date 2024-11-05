package org.example.notion.app.teamUser

import org.example.notion.app.teamUser.dto.TeamUserCreateDto
import org.example.notion.app.teamUser.dto.TeamUserMyResponseDto
import org.example.notion.app.teamUser.dto.TeamUserResponseDto
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
        @RequestBody teamUserDto: TeamUserCreateDto
    ): ResponseEntity<TeamUserResponseDto> {
        val created = teamUserService.create(teamUserDto)
        return ResponseEntity<TeamUserResponseDto>(created, HttpStatus.CREATED)
    }

    @DeleteMapping
    fun delete(
        @RequestBody teamUserDto: TeamUserCreateDto
    ): ResponseEntity<Void> {
        teamUserService.delete(teamUserDto)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("/{teamId}")
    fun get(
        @PathVariable teamId: Long
    ): ResponseEntity<List<TeamUserResponseDto>> {
        return ResponseEntity.ok(teamUserService.findByTeamId(teamId))
    }

    @GetMapping("/my")
    fun get(
    ): ResponseEntity<List<TeamUserMyResponseDto>> {
        return ResponseEntity.ok(teamUserService.findByUserId())
    }

}