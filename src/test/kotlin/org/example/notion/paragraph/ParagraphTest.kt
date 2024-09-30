package org.example.notion.paragraph

import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.entity.ParagraphType
import org.example.notion.app.user.dto.UserResponseDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.File

class ParagraphTest : AbstractIntegrationTest() {

    lateinit var testUser: UserResponseDto
    lateinit var testNote: NoteDto

    @BeforeEach
    fun setUp() {
        testUser = createUser()
        testNote = createNote(testUser.userId)
    }

    @Test
    fun `delete paragraph - valid paragraph id - success deleted`() {
        val paragraphGetResponseExpected = `create paragraph`()
        val paragraphGetResponseActual = `get paragraph`(paragraphGetResponseExpected.id)
        Assertions.assertEquals(paragraphGetResponseExpected, paragraphGetResponseActual)
        `delete paragraph`(paragraphGetResponseActual.id)
        Assertions.assertThrows(Exception::class.java) { `get paragraph`(paragraphGetResponseActual.id) }
    }

    @Test
    fun `execute paragraph - valid paragraph id - success executed`() {
        val paragraph = `create paragraph`(ParagraphType.PYTHON_PARAGRAPH, "print('Hello, World!')")
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/paragraph/execute/${paragraph.id}")
        )
            .andExpect(MockMvcResultMatchers.request().asyncStarted())  // Убедиться, что запрос асинхронный
            .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Hello, World!\n"))
        `delete paragraph`(paragraph.id)
    }

    @Test
    fun `execute paragraph with logarithmic logic - valid paragraph id - success executed`() {
        val pythonCode = """
        import math
        number = 100
        result = math.log10(number)
        print(result)
        """.trimIndent()

        val paragraph = `create paragraph`(ParagraphType.PYTHON_PARAGRAPH, pythonCode)

        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/paragraph/execute/${paragraph.id}")
        )
            .andExpect(MockMvcResultMatchers.request().asyncStarted())  // Убедиться, что запрос асинхронный
            .andReturn()

        // Ожидание завершения асинхронного выполнения
        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(MockMvcResultMatchers.status().isOk)  // Проверяем статус
            .andExpect(MockMvcResultMatchers.content().string("2.0\n"))  // Ожидаем результат
        `delete paragraph`(paragraph.id)
    }

    @Test
    fun `update paragraph - valid data - success update`() {
        val paragraphGetResponseExpected = `create paragraph`()
        val paragraphGetResponseActual = `get paragraph`(paragraphGetResponseExpected.id)
        Assertions.assertEquals(paragraphGetResponseExpected, paragraphGetResponseActual)

        val paragraphUpdateRequest = ParagraphUpdateRequest(
            id = paragraphGetResponseActual.id,
            title = "Updated Title",
            text = "print('Now I am updated!')",
            paragraphType = ParagraphType.PYTHON_PARAGRAPH,
            images = emptyList()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/paragraph/update")
                .param("id", paragraphUpdateRequest.id.toString())
                .param("title", paragraphUpdateRequest.title)
                .param("text", paragraphUpdateRequest.text)
                .param("paragraphType", paragraphUpdateRequest.paragraphType.name)
                .header("userId", testUser.userId)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )

            .andExpect(MockMvcResultMatchers.status().isOk)
        val paragraphGetResponseUpdated = `get paragraph`(paragraphGetResponseActual.id)
        Assertions.assertEquals("Updated Title", paragraphGetResponseUpdated.title)
        Assertions.assertEquals("print('Now I am updated!')", paragraphGetResponseUpdated.text)
        Assertions.assertEquals(ParagraphType.PYTHON_PARAGRAPH, paragraphGetResponseUpdated.paragraphType)
        Assertions.assertEquals(true, paragraphGetResponseUpdated.imageUrls.isEmpty())
        `delete paragraph`(paragraphGetResponseActual.id)
    }


    private fun `get paragraph`(paragraphId: Long): ParagraphGetResponse {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/paragraph/get/$paragraphId")
                .header("userId", testUser.userId)
        ).andReturn().response.contentAsString
            .let { return mapper.readValue(it, ParagraphGetResponse::class.java) }
    }

    private fun `delete paragraph`(paragraphId: Long) {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/paragraph/delete/$paragraphId")
        )
    }

    private fun `create paragraph`(
        paragraphType: ParagraphType = ParagraphType.PLAIN_TEXT_PARAGRAPH,
        paragraphText: String = "This is a test paragraph",
    ): ParagraphGetResponse {
        val file = File("src/test/resources/images/Cat.jpg")
        val image = MockMultipartFile(
            "images",
            file.name,
            MediaType.IMAGE_JPEG_VALUE,
            file.readBytes()
        )

        val paragraphCreateRequest = ParagraphCreateRequest(
            noteId = testNote.noteId,
            title = "Test Title",
            nextParagraphId = null,
            text = paragraphText,
            paragraphType = paragraphType,
            images = listOf(image)
        )

        val result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/paragraph/create")
                .file(image)  // Передача файла
                .param("noteId", paragraphCreateRequest.noteId.toString())
                .param("title", paragraphCreateRequest.title)
                .param("text", paragraphCreateRequest.text)
                .param("paragraphType", paragraphCreateRequest.paragraphType.name)
                .header("userId", testUser.userId)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.noteId").value(paragraphCreateRequest.noteId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(paragraphCreateRequest.title))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.nextParagraphId").value(paragraphCreateRequest.nextParagraphId)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(paragraphCreateRequest.text))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.paragraphType").value(paragraphCreateRequest.paragraphType.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.imageUrls").isNotEmpty)
            .andReturn().response.contentAsString
        return mapper.readValue(result, ParagraphGetResponse::class.java)
    }
}