package org.example.notion.app.team.repository

import org.example.notion.app.team.entity.Team
import org.springframework.data.repository.CrudRepository


interface TeamRepository : CrudRepository<Team, Long> {
    fun findTeamByTeamId(id: Long): Team?
    fun findTeamByName(name: String): Team?
    fun findByOwner(id: Long): List<Team>
}