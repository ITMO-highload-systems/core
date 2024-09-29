package org.example.notion.app.repository

import org.example.notion.app.dto.TeamDto
import org.example.notion.app.entity.Team
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Component

@Component
class TeamRepository(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
) {

    companion object {
        private const val SELECT_FROM_TEAM = """
            select
                team_id,
                name,
                owner
        """

        private const val FIND_BY_TEAM_ID = "$SELECT_FROM_TEAM where team_id = :team_id;"
        private const val FIND_BY_NAME = "$SELECT_FROM_TEAM where name = :name;"

        private const val DELETE_BY_TEAM_ID = "delete from team where team_id = :team_id;"

        private const val UPDATE_TEAM = "update team set name = :name, owner = :owner where team_id = :team_id;"

        private const val INSERT_INTO_TEAM = "insert into team (name, owner) values (:name, :owner);"
    }

    private val rowMapper: RowMapper<Team> = RowMapper { rs, _ ->
        Team(
            teamId = rs.getInt("team_id"),
            name = rs.getString("name"),
            owner = rs.getInt("owner")
        )
    }

    fun findByTeamId(teamId: Int): Team? =
        namedParameterJdbcOperations.query(
            FIND_BY_TEAM_ID,
            mapOf("team_id" to teamId),
            rowMapper
        ).singleOrNull()

    fun findByName(name: String): Team? =
        namedParameterJdbcOperations.query(
            FIND_BY_NAME,
            mapOf("name" to name),
            rowMapper
        ).singleOrNull()

    fun update(teamDto: TeamDto): Int =
        namedParameterJdbcOperations.update(
            UPDATE_TEAM,
            mapOf(
                "name" to teamDto.name,
                "owner" to teamDto.owner
            ),
        )

    fun deleteByTeamId(teamId: Int): Int =
        namedParameterJdbcOperations.update(
            DELETE_BY_TEAM_ID,
            mapOf("team_id" to teamId),
        )

    fun save(teamDto: TeamDto): Int =
        namedParameterJdbcOperations.update(
            INSERT_INTO_TEAM,
            mapOf(
                "name" to teamDto.name,
                "owner" to teamDto.owner
            )
        )
}