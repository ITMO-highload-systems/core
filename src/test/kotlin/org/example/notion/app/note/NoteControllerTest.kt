package org.example.notion.app.note

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION
import com.jayway.jsonpath.JsonPath
import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.userPermission.entity.Permission
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NoteControllerTest : AbstractIntegrationTest() {

    companion object {
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    lateinit var testUser: String
    lateinit var testUserToken: String

    @BeforeEach
    fun setUp() {
        testUser = createUser()
        testUserToken = signInAs(testUser)
    }

    @Test
    fun `create note - valid params - success create`() {
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .content(mapper.writeValueAsString(mapOf("title" to "title", "description" to "description")))
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("title")).andExpect(jsonPath("$.description").value("description"))
            .andExpect(jsonPath("$.owner").value(testUser)).andReturn()

        val createdAtStr = JsonPath.read<String>(response.response.contentAsString, "$.created_at")
        val updatedAtStr = JsonPath.read<String>(response.response.contentAsString, "$.updated_at")

        val createdAt = LocalDateTime.parse(createdAtStr)
        val updatedAt = LocalDateTime.parse(updatedAtStr)

        assert(createdAtStr.isNotEmpty()) { "createdAt should not be empty" }
        assert(updatedAtStr.isNotEmpty()) { "updatedAt should not be empty" }

        assertTrue(updatedAt.minusSeconds(createdAt.second.toLong()).second < 1)
    }

    @Test
    fun `create note - title null - failed create`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
                .content(mapper.writeValueAsString(mapOf("title" to null, "description" to "description")))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `update note - note not exist - not found update`() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "noteId" to "123", "owner" to "123", "title" to "title", "description" to "description"
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `update note - valid params - success update`() {
        val note = createNote(testUserToken)
        val newTitle = note.title + "1"
        val newDescription = note.description + "1"
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to note.noteId,
                            "owner" to note.owner,
                            "title" to newTitle,
                            "description" to newDescription
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value(newTitle)).andExpect(jsonPath("$.description").value(newDescription))
            .andExpect(jsonPath("$.owner").value(note.owner))
            .andExpect(jsonPath("$.updated_at", not(`is`(note.updatedAt.format(FORMATTER)))))
    }

    @Test
    fun `update note - owner change not existing owner - failed update`() {
        val note = createNote(testUserToken)
        val newTitle = note.title + "1"
        val newDescription = note.description + "1"
        val notExistingNewOwner = "12345"
        mockIsUserExist(notExistingNewOwner, false)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to note.noteId,
                            "owner" to notExistingNewOwner,
                            "title" to newTitle,
                            "description" to newDescription
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `update note - owner change existing owner - success update`() {
        val note = createNote(testUserToken)
        val newTitle = note.title + "1"
        val newDescription = note.description + "1"
        val existingNewOwner = createUser()
        val existingToken = signInAs(existingNewOwner)
        mockIsUserExist(existingNewOwner, true)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to note.noteId,
                            "owner" to existingNewOwner,
                            "title" to newTitle,
                            "description" to newDescription
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.note_id").value(note.noteId)).andExpect(jsonPath("$.title").value(newTitle))
            .andExpect(jsonPath("$.description").value(newDescription))
            .andExpect(jsonPath("$.owner").value(existingNewOwner))
            .andExpect(jsonPath("$.updated_at", not(`is`(note.updatedAt.format(FORMATTER)))))

        val notes = getNoteById(signInAs(existingNewOwner), note.noteId)

        assertEquals(note.noteId, notes.noteId)
        assertEquals(newTitle, notes.title)
        assertEquals(newDescription, notes.description)
        assertEquals(existingNewOwner, notes.owner)
        assertEquals(note.createdAt, notes.createdAt)
        assertNotEquals(note.updatedAt, notes.updatedAt)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/my")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/my")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $existingToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isNotEmpty)
    }

    @Test
    fun `update note - executor change existing owner - forbidden update`() {
        val note = createNote(testUserToken)
        val newTitle = note.title + "1"
        val newDescription = note.description + "1"
        val executor = UUID.randomUUID().toString()
        createPermission(testUserToken, executor, note.noteId, Permission.EXECUTOR)
        val executorToken = signInAs(executor)
        val newOwner = UUID.randomUUID().toString()
        mockIsUserExist(newOwner, true)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to note.noteId,
                            "owner" to newOwner,
                            "title" to newTitle,
                            "description" to newDescription
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $executorToken")
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `update note - executor valid update - success update`() {
        val note = createNote(testUserToken)
        val newTitle = note.title + "1"
        val newDescription = note.description + "1"
        val executor = UUID.randomUUID().toString()
        createPermission(testUserToken, executor, note.noteId, Permission.EXECUTOR)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to note.noteId,
                            "owner" to note.owner,
                            "title" to newTitle,
                            "description" to newDescription
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value(newTitle)).andExpect(jsonPath("$.description").value(newDescription))
            .andExpect(jsonPath("$.owner").value(note.owner))
            .andExpect(jsonPath("$.updated_at", not(`is`(note.updatedAt.format(FORMATTER)))))
    }

    @Test
    fun `update note - less than executor valid update - forbidden update`() {
        val note = createNote(testUserToken)
        val newTitle = note.title + "1"
        val newDescription = note.description + "1"
        val executor = UUID.randomUUID().toString()
        createPermission(testUserToken, executor, note.noteId, Permission.WRITER)
        val executorToken = signInAs(executor)
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/note")
                .contentType(MediaType.APPLICATION_JSON).content(
                    mapper.writeValueAsString(
                        mapOf(
                            "note_id" to note.noteId,
                            "owner" to note.owner,
                            "title" to newTitle,
                            "description" to newDescription
                        )
                    )
                )
                .header(AUTHORIZATION, "Bearer $executorToken")
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `delete by note id - owner delete not existing note -not found delete`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/note/1234")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete by note id - owner delete existing note - success delete`() {
        val note = createNote(testUserToken)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/note/${note.noteId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `delete by note id - owner delete twice existing note - not found delete`() {
        val note = createNote(testUserToken)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/note/${note.noteId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/note/${note.noteId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `delete by note id - not owner delete existing note -forbidden delete`() {
        val note = createNote(testUserToken)
        val newUser = UUID.randomUUID().toString()
        createPermission(testUserToken, newUser, note.noteId, Permission.WRITER)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/note/${note.noteId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer ${signInAs(newUser)}")
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @WithMockUser
    fun `get note by id - note not exist - not found get`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/123")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

    }


    @Test
    fun `get note by id - valid input - success get`() {
        val note = createNote(testUserToken)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/${note.noteId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.owner").value(note.owner))
            .andExpect(jsonPath("$.note_id").value(note.noteId))
            .andExpect(jsonPath("$.title").value(note.title))
            .andExpect(jsonPath("$.description").value(note.description))
            .andExpect(jsonPath("$.updated_at", `is`(note.updatedAt.format(FORMATTER))))
    }

    @Test
    fun `get note by id - admin - success get`() {
        val token = signInAs(testUser, "ROLE_ADMIN")
        val note = createNote(token)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/${note.noteId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $token")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.owner").value(note.owner))
            .andExpect(jsonPath("$.note_id").value(note.noteId))
            .andExpect(jsonPath("$.title").value(note.title))
            .andExpect(jsonPath("$.description").value(note.description))
            .andExpect(jsonPath("$.updated_at", `is`(note.updatedAt.format(FORMATTER))))
    }

    @Test
    @WithMockUser
    fun `get notes by owner - no notes - success get`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/my")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `get notes by owner - 1 note - success get`() {
        val note = createNote(testUserToken)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/my")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $testUserToken")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isNotEmpty).andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("[0].owner").value(note.owner))
            .andExpect(jsonPath("[0].note_id").value(note.noteId))
            .andExpect(jsonPath("[0].title").value(note.title))
            .andExpect(jsonPath("[0].description").value(note.description))
            .andExpect(jsonPath("[0].updated_at", `is`(note.updatedAt.format(FORMATTER))))
    }

}