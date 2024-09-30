package org.example.notion.app.userPermission

import com.fasterxml.jackson.core.type.TypeReference
import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.user.dto.UserResponseDto
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
    fun `forbidden create permission if no user and note`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/user/permissions")
                .header("user-id", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("userId" to 1, "noteId" to 1L, "permission" to "READER")))
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `forbidden change owner permission`() {
        val userDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(userDtoResponse.userId)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/user/permissions")
                .header("user-id", userDtoResponse.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to userDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId,
                            "permission" to "READER"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `owner allowed create permission`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)

        assertContains(
            getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId),
            NoteUserPermissionDto(someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)
        )
    }


    @Test
    fun `forbidden create permission for not owner`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse2: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/user/permissions")
                .header("user-id", someUserDtoResponse2.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId,
                            "permission" to "READER"
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    //delete

    @Test
    fun `owner allowed delete existing permission`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)

        assertFalse { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId).isEmpty() }

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/user/permissions")
                .header("user-id", ownerDtoResponse.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)

        assertTrue { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId).isEmpty() }
    }

    @Test
    fun `owner allowed delete existing permission twice`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)
        assertFalse { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId).isEmpty() }

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/user/permissions")
                .header("user-id", ownerDtoResponse.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/user/permissions")
                .header("user-id", ownerDtoResponse.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertTrue { getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId).isEmpty() }

    }

    @Test
    fun `owner allowed delete not existing permission`() {
        val ownerDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/user/permissions")
                .header("user-id", ownerDtoResponse.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to "-1",
                            "noteId" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `forbidden delete permission for not owner`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse2: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)


        mockMvc.perform(
            MockMvcRequestBuilders
                .delete("/api/user/permissions")
                .header("user-id", someUserDtoResponse2.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `owner allowed update existing permission`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)

        assertContains(
            getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId),
            NoteUserPermissionDto(someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .put("/api/user/permissions")
                .header("user-id", ownerDtoResponse.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk)

        val notePermission = getNotePermission(noteDtoResponse.noteId, ownerDtoResponse.userId)
        assertContains(
            notePermission,
            NoteUserPermissionDto(someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.WRITER)
        )
        assertEquals(1, notePermission.size)
    }

    @Test
    fun `forbidden update existing permission for not owner`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse2: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)


        mockMvc.perform(
            MockMvcRequestBuilders
                .put("/api/user/permissions")
                .header("user-id", someUserDtoResponse2.userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "userId" to someUserDtoResponse.userId,
                            "noteId" to noteDtoResponse.noteId,
                            "permission" to Permission.WRITER
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `forbidden get permission if no permission`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse2: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)


        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/user/permissions/{noteId}", noteDtoResponse.noteId)
                .header("user-id", someUserDtoResponse2.userId) // Ensure userId is in string format
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `allowed get permission if reader`() {
        val ownerDtoResponse: UserResponseDto = createUser()
        val someUserDtoResponse: UserResponseDto = createUser()

        val noteDtoResponse: NoteDto = createNote(ownerDtoResponse.userId)

        createPermission(ownerDtoResponse.userId, someUserDtoResponse.userId, noteDtoResponse.noteId, Permission.READER)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/user/permissions/{noteId}", noteDtoResponse.noteId)
                .header("user-id", someUserDtoResponse.userId) // Ensure userId is in string format
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    private fun getNotePermission(noteId: Long, userId: Long): List<NoteUserPermissionDto> {
        val contentAsString = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/user/permissions/{noteId}", noteId)
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