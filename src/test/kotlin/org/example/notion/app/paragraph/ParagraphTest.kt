package org.example.notion.app.paragraph

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION
import com.github.tomakehurst.wiremock.client.WireMock
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.example.notion.AbstractIntegrationTest
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.entity.ParagraphType
import org.example.notion.app.paragraph.repository.ParagraphRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ParagraphTest : AbstractIntegrationTest() {

    lateinit var testUser: String
    lateinit var testNote: NoteDto
    lateinit var adminToken: String

    @Autowired
    private lateinit var paragraphRepository: ParagraphRepository

    @Autowired
    lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @BeforeEach
    fun setUp() {
        adminToken = signInAsAdmin(createUser())
        mockImageService.stubFor(
            WireMock.get(WireMock.urlPathMatching("/api/v1/image/\\d+"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                {
                    "image_urls": [
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg"
                    ]
                }
            """.trimIndent()
                        )
                )
        )
        testNote = createNote(adminToken)
        testUser = createUser()
    }

    @AfterEach
    fun clear() {
        paragraphRepository.deleteAll()
    }

    @Test
    fun `delete paragraph - valid paragraph id - success deleted`() {
        mockImageService.stubFor(
            WireMock.delete(WireMock.urlPathMatching("/api/v1/image/by-paragraph/\\d+"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.NO_CONTENT.value())
                )
        )
        val paragraphGetResponseExpected = `create paragraph`()
        val paragraphGetResponseActual = `get paragraph`(paragraphGetResponseExpected.id)
        Assertions.assertEquals(paragraphGetResponseExpected, paragraphGetResponseActual)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/paragraph/${paragraphGetResponseActual.id}")
                .header(AUTHORIZATION, "Bearer $adminToken")
        ).andExpect(status().isNoContent)
        Assertions.assertThrows(Exception::class.java) { `get paragraph`(paragraphGetResponseActual.id) }
    }

    @Test
    fun `execute paragraph - valid paragraph id - success executed`() {
        mockCodeExecService.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/v1/execution/execute"))
                .withQueryParam("paragraphId", WireMock.matching("\\d+"))
                .withQueryParam("code", WireMock.matching(".*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody("Hello, World!\n")
                )
        )
        val paragraph = `create paragraph`(ParagraphType.PYTHON_PARAGRAPH, "print('Hello, World!')")
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/paragraph/execute/${paragraph.id}")
                .header(AUTHORIZATION, "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Hello, World!\n"))
    }

    @Test
    fun `execute paragraph with logarithmic logic - valid paragraph id - success executed`() {
        mockCodeExecService.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/v1/execution/execute"))
                .withQueryParam("paragraphId", WireMock.matching("\\d+"))
                .withQueryParam("code", WireMock.matching(".*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody("2.0\n")
                )
        )
        val pythonCode = """
        import math
        number = 100
        result = math.log10(number)
        print(result)
        """.trimIndent()

        val paragraph = `create paragraph`(ParagraphType.PYTHON_PARAGRAPH, pythonCode)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/paragraph/execute/${paragraph.id}")
                .header(AUTHORIZATION, "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("2.0\n"))
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
            paragraphType = ParagraphType.PYTHON_PARAGRAPH
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/paragraph")
                .header(AUTHORIZATION, "Bearer $adminToken")
                .content(mapper.writeValueAsString(paragraphUpdateRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )

            .andExpect(status().isOk)
        val paragraphGetResponseUpdated = `get paragraph`(paragraphGetResponseActual.id)
        Assertions.assertEquals("Updated Title", paragraphGetResponseUpdated.title)
        Assertions.assertEquals("print('Now I am updated!')", paragraphGetResponseUpdated.text)
        Assertions.assertEquals(ParagraphType.PYTHON_PARAGRAPH, paragraphGetResponseUpdated.paragraphType)
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
            MockMvcRequestBuilders.put("/api/v1/paragraph/position")
                .header(AUTHORIZATION, "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(changeParagraphPositionRequest))
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
            MockMvcRequestBuilders.put("/api/v1/paragraph/position")
                .header(AUTHORIZATION, "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(changeParagraphPositionRequest))
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
            MockMvcRequestBuilders.put("/api/v1/paragraph/position")
                .header(AUTHORIZATION, "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(changeParagraphPositionRequest))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `check circuit breaker`() {
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("default")
        circuitBreaker.reset()
        mockCodeExecService.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/v1/execution/execute"))
                .withQueryParam("paragraphId", WireMock.matching("\\d+"))
                .withQueryParam("code", WireMock.matching(".*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                )
        )
        val paragraph = `create paragraph`(ParagraphType.PYTHON_PARAGRAPH, "print('Hello, World!')")
        // Отправляем запросы и проверяем, что Circuit Breaker переключается в состояние OPEN после порога отказов
        repeat(10) {
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/paragraph/execute/${paragraph.id}")
                    .header(AUTHORIZATION, "Bearer $adminToken")
            ).andExpect(status().isServiceUnavailable)
        }

        // Проверяем, что Circuit Breaker теперь находится в открытом состоянии
        Assertions.assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN, circuitBreaker.state)
    }

    @ParameterizedTest
    @CsvSource(
        // pageNumber, pageSize, expectedSize
        "0, 50, 50",
        "0, 53, 50",
        "1, 27, 25",
    )
    fun `getAllParagraphs - valid data - success get all paragraphs`(
        pageNumber: Long,
        pageSize: Long,
        expectedSize: Int
    ) {
        for (i in 1..52) `create paragraph`()

        val result0 = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/paragraph/all")
                .header(AUTHORIZATION, "Bearer $adminToken")
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
            MockMvcRequestBuilders.get("/api/v1/paragraph/$paragraphId")
                .header(AUTHORIZATION, "Bearer $adminToken")
        ).andReturn().response.contentAsString
            .let { return mapper.readValue(it, ParagraphGetResponse::class.java) }
    }

    private fun `create paragraph`(
        paragraphType: ParagraphType = ParagraphType.PLAIN_TEXT_PARAGRAPH,
        paragraphText: String = "This is a test paragraph",
    ): ParagraphGetResponse {

        val paragraphCreateRequest = ParagraphCreateRequest(
            noteId = testNote.noteId,
            title = "Test Title",
            nextParagraphId = null,
            text = paragraphText,
            paragraphType = paragraphType
        )

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/paragraph")
                .header(AUTHORIZATION, "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(paragraphCreateRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.note_id").value(paragraphCreateRequest.noteId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(paragraphCreateRequest.title))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.next_paragraph_id").value(paragraphCreateRequest.nextParagraphId)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(paragraphCreateRequest.text))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.paragraph_type").value(paragraphCreateRequest.paragraphType.name)
            )
            .andReturn().response.contentAsString
        return mapper.readValue(result, ParagraphGetResponse::class.java)
    }

    // TODO пофиксить добавление картинки / запаковать все в docker / убедится в работоспособности circuit breaker / отрефакторить все по по максимуму чтобы было меньше кода
}