package org.example.notion.app.team

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION
import org.example.notion.AbstractIntegrationTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*
import kotlin.test.Test

class TeamControllerTest : AbstractIntegrationTest() {

    lateinit var testUser: String
    lateinit var testUserToken: String

    @BeforeEach
    fun setUp() {
        testUser = createUser()
        testUserToken = signInAs(testUser)
    }

    @Test
    fun `create team - valid input - success`() {
        val teamName = UUID.randomUUID().toString()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to teamName)))
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(teamName))
            .andExpect(jsonPath("$.owner").value(testUser))
    }

    @Test
    fun `create team - duplicate name - conflict`() {
        val teamName = UUID.randomUUID().toString()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to teamName)))
        ).andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to teamName)))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `create team - invalid input - bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("name" to "")))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `update team - update name - success`() {
        val createdTeam = createTeam(testUser)
        val newName = UUID.randomUUID().toString()
        mockIsUserExist(testUser, true)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
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
        val createdTeam = createTeam(testUser)
        val newName = ""
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
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
        val createdTeam = createTeam(testUser)
        mockIsUserExist(createdUser, true)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to createdTeam.teamId,
                            "owner" to createdUser,
                            "name" to createdTeam.name
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(createdTeam.name))
            .andExpect(jsonPath("$.owner").value(createdUser))
            .andExpect(jsonPath("$.team_id").value(createdTeam.teamId))
    }

    @Test
    fun `update team - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "team_id" to 123,
                            "owner" to testUser,
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
                .header(AUTHORIZATION, "Bearer $testUserToken")
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
        val createTeam1 = createTeam(testUser)
        val createTeam2 = createTeam(testUser)
        mockIsUserExist(testUser, true)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team")
                .header(AUTHORIZATION, "Bearer $testUserToken")
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
        val team = createTeam(testUser)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/" + team.teamId)
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/123")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete team - invalid input - bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/12h3")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `get team by ID - owner get valid input - success`() {
        val team = createTeam(testUser)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/${team.teamId}")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(team.teamId))
            .andExpect(jsonPath("$.name").value(team.name))
            .andExpect(jsonPath("$.owner").value(team.owner))
    }

    @Test
    fun `get team by ID - participant get valid input - success`() {
        val team = createTeam(testUser)
        val createUser = createUser()
        createTeamParticipant(testUserToken, createUser, team.teamId)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/${team.teamId}")
                .header(AUTHORIZATION, "Bearer $testUserToken")
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
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `get team by ID - user not in team - forbidden`() {
        val team = createTeam(testUser)
        val newUser = createUser()
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/${team.teamId}")
                .header(AUTHORIZATION, "Bearer ${signInAs(newUser)}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `get team my teams - no teams - success`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/my")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `get team my teams - 1 team - success`() {
        val createTeam = createTeam(testUser)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/my")
                .header(AUTHORIZATION, "Bearer $testUserToken")
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
        val createTeam1 = createTeam(testUser)
        val createTeam2 = createTeam(testUser)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/my")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(2)))
            .andExpect(jsonPath("[0].team_id").value(createTeam1.teamId))
            .andExpect(jsonPath("[1].team_id").value(createTeam2.teamId))
    }


}