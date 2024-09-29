package org.example.notion.app.controller

import jakarta.validation.Valid
import org.example.notion.app.dto.TeamDto
import org.example.notion.app.service.TeamService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController("api/team")
class TeamController(
    private val teamService: TeamService
) {

    @GetMapping("{teamId}")
    fun getByTeamId(@PathVariable("teamId") teamId: Int): ResponseEntity<TeamDto> {
        return ResponseEntity.ok().body(teamService.getByTeamId(teamId))
    }

    @GetMapping("name/{name}")
    fun getByName(@PathVariable("name") name: String): ResponseEntity<TeamDto> {
        return ResponseEntity.ok().body(teamService.getByName(name))
    }

    @PostMapping()
    fun createTeam(@Valid @RequestBody teamDto: TeamDto): ResponseEntity<Unit> {
        teamService.createTeam(teamDto)
        return ResponseEntity.ok().build()
    }

    @PutMapping()
    fun updateTeam(@Valid @RequestBody teamDto: TeamDto): ResponseEntity<Int> {
        return ResponseEntity.ok().body(teamService.update(teamDto))
    }

    @DeleteMapping("{teamId}")
    fun deleteTeam(@PathVariable("teamId") teamId: Int): ResponseEntity<Unit> {
        teamService.deleteByTeamId(teamId)
        return ResponseEntity.ok().build()
    }


}