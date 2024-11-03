package org.example.notion.app.team

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION
import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.team.dto.TeamDto
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import kotlin.test.Test

class TeamUserControllerTest : AbstractIntegrationTest() {
    lateinit var testUser: String
    lateinit var testUserToken: String
    lateinit var testTeam: TeamDto

    @BeforeEach
    fun setUp() {
        testUser = createUser()
        testUserToken = signInAs(testUser)
        testTeam = createTeam(testUser)
    }

    @Test
    fun `create team user - valid input owner participant - success`() {
        mockIsUserExist(testUser, true)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to testUser,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(testTeam.teamId))
            .andExpect(jsonPath("$.user_id").value(testUser))
    }

    @Test
    fun `create team user - valid input new participant - success`() {
        val newUser = createUser()
        mockIsUserExist(newUser, true)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to newUser,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(testTeam.teamId))
            .andExpect(jsonPath("$.user_id").value(newUser))
    }

    @Test
    fun `create team user - user not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to 123,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `create team user - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to testUser,
                            "team_id" to 123
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `create team user - not owner add participant - forbidden`() {
        mockIsUserExist(testUser, true)
        val user = createUser()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer ${signInAs(user)}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to testUser,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `delete team user - valid input - success`() {
        val newUser = createUser()
        createTeamParticipant(testUserToken, newUser, testTeam.teamId)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to newUser,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team user - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to testUser,
                            "team_id" to 123
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete team user - user not exist - success`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to 123,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team user - participant not exist - success`() {
        val newUser = createUser()
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to newUser,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team user - not owner remove participant - forbidden`() {
        val newUser = createUser()
        createTeamParticipant(testUserToken, testUser, testTeam.teamId)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/user")
                .header(AUTHORIZATION, "Bearer ${signInAs(newUser)}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to newUser,
                            "team_id" to testTeam.teamId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `find by team id - team not exist - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/user/123")
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `find by team id - team exist, no participant - success`() {
        val team = createTeam(testUser)
        mockMvc.perform(
            MockMvcRequestBuilders
                .get(
                    "/api/v1/team/user/${team.teamId}"
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$").isEmpty)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `find by team id - team exist ok - not found`() {
        val team = createTeam(testUser)
        mockMvc.perform(
            MockMvcRequestBuilders
                .get(
                    "/api/v1/team/user/${team.teamId}"
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$").isEmpty)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `find by user id - no teams - not found`() {
        val newUser = createUser()
        mockMvc.perform(
            MockMvcRequestBuilders
                .get(
                    "/api/v1/team/user/my"
                )
                .header(AUTHORIZATION, "Bearer ${signInAs(newUser)}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$").isEmpty)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `find by user id - some teams - success`() {
        val newUser = createUser()
        createTeamParticipant(testUserToken, newUser, testTeam.teamId)
        mockMvc.perform(
            MockMvcRequestBuilders
                .get(
                    "/api/v1/team/user/my"
                )
                .header(AUTHORIZATION, "Bearer ${signInAs(newUser)}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("[0].team_id").value(testTeam.teamId))
    }
}