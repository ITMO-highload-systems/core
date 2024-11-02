package org.example.notion.app.userPermission

import com.fasterxml.jackson.core.type.TypeReference
import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.userPermission.dto.NoteUserPermissionDto
import org.example.notion.app.userPermission.entity.Permission
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPermissionTest : AbstractIntegrationTest() {

    //create
    @Test
    fun `not found create permission if no user and note`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/user/permissions")
                .header("user-id", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("userId" to 1, "noteId" to 1L, "permission" to "READER")))
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `forbidden change owner permission`() {
        val userDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/user/permissions")
                .header("user-id", userDtoResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to userDtoResponse,
                            "note_id" to noteDtoResponse.noteId,
                            "permission" to "READER"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `owner allowed create permission`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)

        assertContains(
            getNotePermission(noteDtoResponse.noteId, ownerDtoResponse),
            NoteUserPermissionDto(someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)
        )
    }


    @Test
    fun `forbidden create permission for not owner`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()
        val someUserDtoResponse2: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/user/permissions")
                .header("user-id", someUserDtoResponse2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId,
                            "permission" to "READER"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    //delete

    @Test
    fun `owner allowed delete existing permission`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)

        assertFalse { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse).isEmpty() }

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/v1/user/permissions")
                .header("user-id", ownerDtoResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)

        assertTrue { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse).isEmpty() }
    }

    @Test
    fun `owner allowed delete existing permission twice`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)
        assertFalse { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse).isEmpty() }

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/v1/user/permissions")
                .header("user-id", ownerDtoResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/v1/user/permissions")
                .header("user-id", ownerDtoResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertTrue { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse).isEmpty() }

    }

    @Test
    fun `owner allowed delete not existing permission`() {
        val ownerDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/v1/user/permissions")
                .header("user-id", ownerDtoResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to "-1",
                            "note_id" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `forbidden delete permission for not owner`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()
        val someUserDtoResponse2: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)


        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/v1/user/permissions")
                .header("user-id", someUserDtoResponse2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `owner allowed update existing permission`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)

        assertContains(
            getNotePermission(noteDtoResponse.noteId, ownerDtoResponse),
            NoteUserPermissionDto(someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .put("/api/v1/user/permissions")
                .header("user-id", ownerDtoResponse)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)

        val notePermission = getNotePermission(noteDtoResponse.noteId, ownerDtoResponse)
        assertContains(
            notePermission,
            NoteUserPermissionDto(someUserDtoResponse, noteDtoResponse.noteId, Permission.WRITER)
        )
        assertEquals(1, notePermission.size)
    }

    @Test
    fun `forbidden update existing permission for not owner`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()
        val someUserDtoResponse2: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)


        mockMvc.perform(
            MockMvcRequestBuilders
                .put("/api/v1/user/permissions")
                .header("user-id", someUserDtoResponse2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to someUserDtoResponse,
                            "note_id" to noteDtoResponse.noteId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `forbidden get permission if no permission`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()
        val someUserDtoResponse2: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)


        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/v1/user/permissions/{noteId}", noteDtoResponse.noteId)
                .header("user-id", someUserDtoResponse2) // Ensure userId is in string format
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `allowed get permission if reader`() {
        val ownerDtoResponse: String = createUser()
        val someUserDtoResponse: String = createUser()

        val noteDtoResponse: NoteDto = createNote()

        createPermission(ownerDtoResponse, someUserDtoResponse, noteDtoResponse.noteId, Permission.READER)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/v1/user/permissions/{noteId}", noteDtoResponse.noteId)
                .header("user-id", someUserDtoResponse) // Ensure userId is in string format
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    private fun getNotePermission(noteId: Long, userId: String): List<NoteUserPermissionDto> {
        val contentAsString = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/v1/user/permissions/{noteId}", noteId)
                .header("user-id", userId.toString()) // Ensure userId is in string format
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response
            .contentAsString

        // Use TypeReference to read the JSON response directly into a list of NoteUserPermissionDto
        return mapper.readValue(contentAsString, object : TypeReference<List<NoteUserPermissionDto>>() {})
    }

}