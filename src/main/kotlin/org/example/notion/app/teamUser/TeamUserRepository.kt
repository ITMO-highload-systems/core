package org.example.notion.app.teamUser

import org.example.notion.app.teamUser.entity.TeamUser
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface TeamUserRepository : CrudRepository<TeamUser, Long> {

    fun findTeamUserByTeamIdAndUserId(teamId: Long, userId: Long): TeamUser?

    @Query("DELETE FROM team_user WHERE user_id = :userId AND team_id = :teamId")
    @Modifying
    fun deleteTeamUserByTeamIdAndUserId(teamId: Long, userId: Long)

    fun findAllByTeamId(teamId: Long): List<TeamUser>
    fun findAllByUserId(userId: Long): List<TeamUser>

}