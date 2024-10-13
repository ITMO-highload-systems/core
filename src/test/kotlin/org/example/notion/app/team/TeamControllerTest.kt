package org.example.notion.app.team

import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.user.dto.UserResponseDto
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*
import kotlin.test.Test

class TeamControllerTest : AbstractIntegrationTest() {

    lateinit var testUser: UserResponseDto

    @BeforeEach
    fun setUp() {
        testUser = createUser()
    }

    @Test
    fun `create team - valid input - success`() {
        val teamName = UUID.randomUUID().toString()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to teamName)))
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(teamName))
            .andExpect(jsonPath("$.owner").value(testUser.userId))
    }

    @Test
    fun `create team - duplicate name - conflict`() {
        val teamName = UUID.randomUUID().toString()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to teamName)))
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to teamName)))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `create team - invalid input - bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to "")))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `update team - update name - success`() {
        val createdTeam = createTeam(testUser.userId)
        val newName = UUID.randomUUID().toString()
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to createdTeam.teamId,
                            "owner" to createdTeam.owner,
                            "name" to newName
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(newName))
            .andExpect(jsonPath("$.owner").value(createdTeam.owner))
            .andExpect(jsonPath("$.team_id").value(createdTeam.teamId))
    }

    @Test
    fun `update team - update incorrect name - bad request`() {
        val createdTeam = createTeam(testUser.userId)
        val newName = ""
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to createdTeam.teamId,
                            "owner" to createdTeam.owner,
                            "name" to newName
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `update team - update owner - success`() {
        val createdUser = createUser()
        val createdTeam = createTeam(testUser.userId)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to createdTeam.teamId,
                            "owner" to createdUser.userId,
                            "name" to createdTeam.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(createdTeam.name))
            .andExpect(jsonPath("$.owner").value(createdUser.userId))
            .andExpect(jsonPath("$.team_id").value(createdTeam.teamId))
    }

    @Test
    fun `update team - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to 123,
                            "owner" to testUser.userId,
                            "name" to "name"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `update team - owner not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to 123,
                            "owner" to 123,
                            "name" to "name"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `update team - name dublicated - conflict`() {
        val createTeam1 = createTeam(testUser.userId)
        val createTeam2 = createTeam(testUser.userId)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to createTeam1.teamId,
                            "owner" to createTeam1.owner,
                            "name" to createTeam2.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `delete team - valid input - success`() {
        val team = createTeam(testUser.userId)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/" + team.teamId)
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/123")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete team - invalid input - bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/12h3")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `get team by ID - owner get valid input - success`() {
        val team = createTeam(testUser.userId)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/${team.teamId}")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(team.teamId))
            .andExpect(jsonPath("$.name").value(team.name))
            .andExpect(jsonPath("$.owner").value(team.owner))
    }

    @Test
    fun `get team by ID - participant get valid input - success`() {
        val team = createTeam(testUser.userId)
        val createUser = createUser()
        createTeamParticipant(testUser.userId, createUser.userId, team.teamId)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/${team.teamId}")
                .header("user-id", createUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(team.teamId))
            .andExpect(jsonPath("$.name").value(team.name))
            .andExpect(jsonPath("$.owner").value(team.owner))
    }

    @Test
    fun `get team by ID - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/123")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `get team by ID - user not in team - forbidden`() {
        val team = createTeam(testUser.userId)
        val newUser = createUser()
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/${team.teamId}")
                .header("user-id", newUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `get team my teams - no teams - success`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/my")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `get team my teams - 1 team - success`() {
        val createTeam = createTeam(testUser.userId)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/my")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("[0].team_id").value(createTeam.teamId))
            .andExpect(jsonPath("[0].owner").value(createTeam.owner))
            .andExpect(jsonPath("[0].name").value(createTeam.name))
    }

    @Test
    fun `get team my teams - 2 team - success`() {
        val createTeam1 = createTeam(testUser.userId)
        val createTeam2 = createTeam(testUser.userId)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/my")
                .header("user-id", testUser.userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(2)))
            .andExpect(jsonPath("[0].team_id").value(createTeam1.teamId))
            .andExpect(jsonPath("[1].team_id").value(createTeam2.teamId))
    }


}