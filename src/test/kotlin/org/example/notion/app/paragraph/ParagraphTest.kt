package org.example.notion.app.paragraph

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.entity.ParagraphType
import org.example.notion.app.paragraph.repository.ImageRepository
import org.example.notion.app.paragraph.repository.ParagraphRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File

class ParagraphTest : AbstractIntegrationTest() {

    lateinit var testUser: String
    lateinit var testNote: NoteDto

    @Autowired
    private lateinit var paragraphRepository: ParagraphRepository

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        testUser = createUser()
        testNote = createNote()
    }

    @AfterEach
    fun clear() {
        imageRepository.deleteAll()
        paragraphRepository.deleteAll()
    }

    @Test
    fun `delete paragraph - valid paragraph id - success deleted`() {
        val paragraphGetResponseExpected = `create paragraph`()
        val paragraphGetResponseActual = `get paragraph`(paragraphGetResponseExpected.id)
        val image = imageRepository.findByParagraphId(paragraphGetResponseActual.id).first()
        Assertions.assertEquals(paragraphGetResponseExpected, paragraphGetResponseActual)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/paragraph/delete/${paragraphGetResponseActual.id}")
                .header("user-id", testUser)
        )
        Assertions.assertThrows(Exception::class.java) { `get paragraph`(paragraphGetResponseActual.id) }
        Assertions.assertNull(imageRepository.findByImageHash(image.imageHash))
    }

    @Test
    fun `delete paragraph - 2 paragraphs with same images - images not deleted`() {
        val paragraphGetResponse1 = `create paragraph`()
        val paragraphGetResponse2 = `create paragraph`()

        val image1 = imageRepository.findByParagraphId(paragraphGetResponse1.id).first()
        val image2 = imageRepository.findByParagraphId(paragraphGetResponse2.id).first()

        Assertions.assertEquals(image1.imageHash, image2.imageHash)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/paragraph/delete/${paragraphGetResponse1.id}")
                .header("user-id", testUser)
        )
        Assertions.assertNotNull(imageRepository.findByImageHash(image1.imageHash))
    }

    @Test
    fun `execute paragraph - valid paragraph id - success executed`() {
        val paragraph = `create paragraph`(ParagraphType.PYTHON_PARAGRAPH, "print('Hello, World!')")
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/paragraph/execute/${paragraph.id}")
                .header("user-id", testUser)
        )
            .andExpect(MockMvcResultMatchers.request().asyncStarted())  // Убедиться, что запрос асинхронный
            .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Hello, World!\n"))
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
            MockMvcRequestBuilders.get("/api/v1/paragraph/execute/${paragraph.id}")
                .header("user-id", testUser)
        )
            .andExpect(MockMvcResultMatchers.request().asyncStarted())  // Убедиться, что запрос асинхронный
            .andReturn()

        // Ожидание завершения асинхронного выполнения
        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(status().isOk)  // Проверяем статус
            .andExpect(MockMvcResultMatchers.content().string("2.0\n"))  // Ожидаем результат
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
            MockMvcRequestBuilders.put("/api/v1/paragraph/update")
                .param("id", paragraphUpdateRequest.id.toString())
                .param("title", paragraphUpdateRequest.title)
                .param("text", paragraphUpdateRequest.text)
                .param("paragraphType", paragraphUpdateRequest.paragraphType.name)
                .header("user-id", testUser)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )

            .andExpect(status().isOk)
        val paragraphGetResponseUpdated = `get paragraph`(paragraphGetResponseActual.id)
        Assertions.assertEquals("Updated Title", paragraphGetResponseUpdated.title)
        Assertions.assertEquals("print('Now I am updated!')", paragraphGetResponseUpdated.text)
        Assertions.assertEquals(ParagraphType.PYTHON_PARAGRAPH, paragraphGetResponseUpdated.paragraphType)
        Assertions.assertEquals(true, paragraphGetResponseUpdated.imageUrls.isEmpty())
    }

    @Test
    fun `change position - 12 - 21 - success change`() {
        val paragraphGetResponse1 = `create paragraph`() // на 1-м месте
        val paragraphGetResponse2 = `create paragraph`() // на 2-м месте

        val changeParagraphPositionRequest = ChangeParagraphPositionRequest(paragraphGetResponse1.id, null)

        val paragraph2before = paragraphRepository.findByParagraphId(paragraphGetResponse2.id)
        Assertions.assertNotNull(paragraph2before) // последующее использование !! оправдано
        Assertions.assertEquals(
            null,
            paragraph2before!!.nextParagraphId
        ) // paragraph2 должен оказаться на последнем месте

        val paragraph1before = paragraphRepository.findByParagraphId(paragraphGetResponse1.id)
        Assertions.assertNotNull(paragraph1before) // последующее использования !! оправдано
        Assertions.assertEquals(
            paragraph2before.id,
            paragraph1before!!.nextParagraphId
        ) // paragraph1 должен стоять перед paragraph2

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/paragraph/position")
                .header("user-id", testUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeParagraphPositionRequest))
        ).andExpect(status().isOk)

        val paragraph2after = paragraphRepository.findByParagraphId(paragraphGetResponse2.id)
        Assertions.assertNotNull(paragraph2after) // последующее использование !! оправдано
        Assertions.assertEquals(
            paragraphGetResponse1.id,
            paragraph2after!!.nextParagraphId
        ) // paragraph2 должен оказаться на 1-м месте

        val paragraph1after = paragraphRepository.findByParagraphId(paragraphGetResponse1.id)
        Assertions.assertNotNull(paragraph1after) // последующее использования !! оправдано
        Assertions.assertEquals(null, paragraph1after!!.nextParagraphId) // paragraph1 должен стоять на последнем месте
    }

    @Test
    fun `change position - 123 - 132 - success change`() {
        val paragraphGetResponse1 = `create paragraph`() // на 1-м месте
        val paragraphGetResponse2 = `create paragraph`() // на 2-м месте
        val paragraphGetResponse3 = `create paragraph`() // на 3-м месте

        val changeParagraphPositionRequest =
            ChangeParagraphPositionRequest(paragraphGetResponse3.id, paragraphGetResponse2.id)

        val paragraph3before = paragraphRepository.findByParagraphId(paragraphGetResponse3.id)
        Assertions.assertNotNull(paragraph3before) // последующее использование !! оправдано
        Assertions.assertEquals(null, paragraph3before!!.nextParagraphId) // paragraph3 должен стоять на последнем месте

        val paragraph2before = paragraphRepository.findByParagraphId(paragraphGetResponse2.id)
        Assertions.assertNotNull(paragraph2before) // последующее использование !! оправдано
        Assertions.assertEquals(
            paragraph3before.id,
            paragraph2before!!.nextParagraphId
        ) // paragraph2 должен стоять перед paragraph3

        val paragraph1before = paragraphRepository.findByParagraphId(paragraphGetResponse1.id)
        Assertions.assertNotNull(paragraph1before) // последующее использования !! оправдано
        Assertions.assertEquals(
            paragraph2before.id,
            paragraph1before!!.nextParagraphId
        ) // paragraph1 должен стоять перед paragraph2

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/paragraph/position")
                .header("user-id", testUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeParagraphPositionRequest))
        ).andExpect(status().isOk)

        val paragraph3after = paragraphRepository.findByParagraphId(paragraphGetResponse3.id)
        Assertions.assertNotNull(paragraph3after) // последующее использование !! оправдано
        Assertions.assertEquals(
            paragraphGetResponse2.id,
            paragraph3after!!.nextParagraphId
        ) // paragraph3 должен стоять перед paragraph2

        val paragraph2after = paragraphRepository.findByParagraphId(paragraphGetResponse2.id)
        Assertions.assertNotNull(paragraph2after) // последующее использование !! оправдано
        Assertions.assertEquals(null, paragraph2after!!.nextParagraphId) // paragraph2 должен стоять последним

        val paragraph1after = paragraphRepository.findByParagraphId(paragraphGetResponse1.id)
        Assertions.assertNotNull(paragraph1after) // последующее использования !! оправдано
        Assertions.assertEquals(
            paragraph3after.id,
            paragraph1after!!.nextParagraphId
        ) // paragraph1 должен стоять перед paragraph3
    }

    @Test
    fun `change position - same ids - bad request`() {
        val changeParagraphPositionRequest =
            ChangeParagraphPositionRequest(1L, 1L)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/paragraph/position")
                .header("user-id", testUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changeParagraphPositionRequest))
        ).andExpect(status().isBadRequest)
    }

    @ParameterizedTest
    @CsvSource(
        // pageNumber, pageSize, expectedSize
        "0, 50, 50",
        "0, 53, 50",
        "1, 27, 25",
    )
    fun `getAllParagraphs - valid data - success get all paragraphs`(pageNumber: Long, pageSize: Long, expectedSize: Int) {
        for (i in 1..52) `create paragraph`()

        val result0 = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/paragraph/all")
                .param("page", pageNumber.toString())
                .param("pageSize", pageSize.toString())
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val paragraphs = mapper.readValue(result0, Array<ParagraphGetResponse>::class.java)
        Assertions.assertEquals(expectedSize, paragraphs.size)
    }


    private fun `get paragraph`(paragraphId: Long): ParagraphGetResponse {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/paragraph/get/$paragraphId")
                .header("user-id", testUser)
        ).andReturn().response.contentAsString
            .let { return mapper.readValue(it, ParagraphGetResponse::class.java) }
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
            MockMvcRequestBuilders.multipart("/api/v1/paragraph/create")
                .file(image)  // Передача файла
                .param("noteId", paragraphCreateRequest.noteId.toString())
                .param("title", paragraphCreateRequest.title)
                .param("text", paragraphCreateRequest.text)
                .param("paragraphType", paragraphCreateRequest.paragraphType.name)
                .header("user-id", testUser)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.note_id").value(paragraphCreateRequest.noteId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(paragraphCreateRequest.title))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.next_paragraph_id").value(paragraphCreateRequest.nextParagraphId)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(paragraphCreateRequest.text))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.paragraph_type").value(paragraphCreateRequest.paragraphType.name)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.image_urls").isNotEmpty)
            .andReturn().response.contentAsString
        return mapper.readValue(result, ParagraphGetResponse::class.java)
    }
}