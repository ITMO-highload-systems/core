package org.example.notion.app.team

import jakarta.validation.Valid
import org.example.notion.app.user.UserContext
import org.example.notion.app.team.dto.TeamCreateDto
import org.example.notion.app.team.dto.TeamDto
import org.example.notion.app.team.service.TeamService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/team")
class TeamController(
    private val teamService: TeamService
) {

    @GetMapping("{teamId}")
    fun getByTeamId(@PathVariable("teamId") teamId: Long,  @RequestHeader("user-id") userId: Long): ResponseEntity<TeamDto> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok().body(teamService.getByTeamId(teamId))
    }

    @GetMapping("name/{name}")
    fun getByName(@PathVariable("name") name: String,  @RequestHeader("user-id") userId: Long): ResponseEntity<TeamDto> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok().body(teamService.getByName(name))
    }

    @PostMapping
    fun createTeam(@Valid @RequestBody teamCreateDto: TeamCreateDto, @RequestHeader("user-id") userId: Long): ResponseEntity<Unit> {
        UserContext.setCurrentUser(userId)
        teamService.createTeam(teamCreateDto)
        return ResponseEntity.ok().build()
    }

    @PutMapping
    fun updateTeam(@Valid @RequestBody teamDto: TeamDto,  @RequestHeader("user-id") userId: Long): ResponseEntity<TeamDto> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok().body(teamService.update(teamDto))
    }

    @DeleteMapping("{teamId}")
    fun deleteTeam(@PathVariable("teamId") teamId: Long,  @RequestHeader("user-id") userId: Long): ResponseEntity<Void> {
        UserContext.setCurrentUser(userId)
        teamService.deleteByTeamId(teamId)
        return ResponseEntity.ok().build()
    }


}