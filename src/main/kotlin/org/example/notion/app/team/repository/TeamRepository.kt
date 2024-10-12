package org.example.notion.app.team.repository

import org.example.notion.app.team.entity.Team
import org.springframework.data.repository.CrudRepository
import java.util.*


interface TeamRepository : CrudRepository<Team, Long> {
    fun findTeamByTeamId(id: Long): Optional<Team>
    fun findTeamByName(name: String): Optional<Team>
    fun findByOwner(id: Long): List<Team>
}