package org.example.notion.app.team

import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.team.dto.TeamDto
import org.example.notion.app.userPermission.dto.NoteTeamPermissionDto
import org.example.notion.app.userPermission.entity.Permission
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class TeamPermissionControllerTest : AbstractIntegrationTest() {
    lateinit var testTeamOwner: String
    lateinit var testNoteOwner: String
    lateinit var testTeam: TeamDto
    lateinit var testNote: NoteDto

    @BeforeEach
    fun setUp() {
        testTeamOwner = createUser()
        testNoteOwner = createUser()
        testTeam = createTeam(testTeamOwner)
        testNote = createNote()
    }

    @Test
    fun `create team permission - valid input  - success`() {

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to testTeam.teamId,
                            "permission" to Permission.READER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(testTeam.teamId))
            .andExpect(jsonPath("$.note_id").value(testNote.noteId))
            .andExpect(jsonPath("$.permission").value(Permission.READER.name))
    }

    @Test
    fun `create team permission - created not by note owner  - forbidden`() {

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/permissions")
                .header("user-id", testTeamOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to testTeam.teamId,
                            "permission" to Permission.READER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `create team permission - permission already exist  - conflict`() {
        createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(testTeam.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to testTeam.teamId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun `create team permission - note not exist  - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to 123,
                            "team_id" to testTeam.teamId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `create team permission - team not exist  - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to 123,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `update team permission - valid input update permission - success`() {
        val team = createTeam(testTeamOwner)
        createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.team_id").value(team.teamId))
            .andExpect(jsonPath("$.note_id").value(testNote.noteId))
            .andExpect(jsonPath("$.permission").value(Permission.WRITER.name))
    }

    @Test
    fun `update team permission - not by note owner for  - success`() {
        val team = createTeam(testTeamOwner)
        createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team/permissions")
                .header("user-id", testTeamOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `update team permission - permission not exist  - not found`() {
        val team = createTeam(testTeamOwner)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `update team permission - note not exist  - not found`() {
        val team = createTeam(testTeamOwner)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to 123,
                            "team_id" to team.teamId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete team permission - delete by note owner  - success`() {
        val team = createTeam(testTeamOwner)
        createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/permissions")
                .header("user-id", testNoteOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team permission - delete by team owner  - success`() {
        val team = createTeam(testTeamOwner)
        createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/permissions")
                .header("user-id", testTeamOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete team permission - delete by some user  - forbidden`() {
        val createUser = createUser()
        val team = createTeam(testTeamOwner)
        createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/permissions")
                .header("user-id", createUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `delete team permission - note not exist  - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/permissions")
                .header("user-id", testTeamOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to 1234,
                            "team_id" to testTeam.teamId,
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete team permission - team not exist  - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/permissions")
                .header("user-id", testTeamOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to 123,
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete team permission - permission not exist  - success`() {
        val team = createTeam(testTeamOwner)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/team/permissions")
                .header("user-id", testTeamOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to testNote.noteId,
                            "team_id" to team.teamId,
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `find by note id - note not exist  - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byNote/123")
                .header("user-id", testNoteOwner)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `find by note id - empty  - success`() {
        val createNote = createNote()
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byNote/${createNote.noteId}")
                .header("user-id", testNoteOwner)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `find by note id - valid  - success`() {
        val note = createNote()
        val createTeamPermission = createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(testTeam.teamId, note.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byNote/${note.noteId}")
                .header("user-id", testNoteOwner)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("[0].team_id").value(createTeamPermission.teamId))
            .andExpect(jsonPath("[0].note_id").value(createTeamPermission.noteId))
            .andExpect(jsonPath("[0].permission").value(createTeamPermission.permission.name))
    }

    @Test
    fun `find by note id - ok for note reader  - success`() {
        val note = createNote()
        val createTeamPermission = createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(testTeam.teamId, note.noteId, Permission.READER)
        )
        val user = createUser()
        createPermission(
            testNoteOwner,
            user,
            note.noteId,
            Permission.READER
        )
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byNote/${note.noteId}")
                .header("user-id", user)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("[0].team_id").value(createTeamPermission.teamId))
            .andExpect(jsonPath("[0].note_id").value(createTeamPermission.noteId))
            .andExpect(jsonPath("[0].permission").value(createTeamPermission.permission.name))
    }

    @Test
    fun `find by note id - forbidden for not note reader  - forbidden`() {
        val note = createNote()
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byNote/${note.noteId}")
                .header("user-id", testTeamOwner)
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `find by team id - team not exist  - not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byTeam/123")
                .header("user-id", testTeamOwner)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `find by team id - empty  - success`() {
        val team = createTeam(testTeamOwner)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byTeam/${team.teamId}")
                .header("user-id", testTeamOwner)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `find by team id - valid  - success`() {
        val team = createTeam(testTeamOwner)
        val createTeamPermission = createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byTeam/${team.teamId}")
                .header("user-id", testTeamOwner)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("[0].team_id").value(createTeamPermission.teamId))
            .andExpect(jsonPath("[0].note_id").value(createTeamPermission.noteId))
            .andExpect(jsonPath("[0].permission").value(createTeamPermission.permission.name))
    }

    @Test
    fun `find by team id - ok for team participant  - success`() {
        val team = createTeam(testTeamOwner)
        val createTeamPermission = createTeamPermission(
            testNoteOwner,
            NoteTeamPermissionDto(team.teamId, testNote.noteId, Permission.READER)
        )
        val user = createUser()
        createTeamParticipant(testTeamOwner, user, team.teamId)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byTeam/${team.teamId}")
                .header("user-id", user)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("[0].team_id").value(createTeamPermission.teamId))
            .andExpect(jsonPath("[0].note_id").value(createTeamPermission.noteId))
            .andExpect(jsonPath("[0].permission").value(createTeamPermission.permission.name))
    }

    @Test
    fun `find by team id - forbidden for not team participant  - forbidden`() {
        val team = createTeam(testTeamOwner)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/team/permissions/byTeam/${team.teamId}")
                .header("user-id", testNoteOwner)
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

}