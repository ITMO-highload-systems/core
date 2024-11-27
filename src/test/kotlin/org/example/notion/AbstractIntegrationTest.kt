package org.example.notion

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.userPermission.entity.Permission
import org.example.notion.config.JwtUtil
import org.example.notion.configuration.ClockTestConfiguration
import org.example.notion.configuration.WireMockConfig
import org.example.notion.kafka.Message
import org.example.notion.kafka.SseService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith()
@ContextConfiguration(classes = [WireMockConfig::class])
abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    @Qualifier("mockSecurityService")
    protected lateinit var mockSecurityService: WireMockServer

    @Autowired
    @Qualifier("mockImageService")
    protected lateinit var mockImageService: WireMockServer

    @Autowired
    @Qualifier("mockCodeExecService")
    protected lateinit var mockCodeExecService: WireMockServer

    @MockBean
    lateinit var sseService: SseService

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        @ServiceConnection
        internal var postgres = PostgreSQLContainer("postgres:latest")

        @BeforeAll
        @JvmStatic
        fun setup() {
            postgres.start()
        }
    }

    @BeforeEach
    fun prepareEnv() {
        ClockTestConfiguration.TestClockProxy.setToFixedClock()
    }

    protected fun createNote(token: String): NoteDto {
        val noteString = mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/note")
                .header(AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("title" to "title", "description" to "description")))
        ).andExpect(MockMvcResultMatchers.status().isCreated).andReturn().response.contentAsString

        val noteDtoResponse: NoteDto = mapper.readValue(noteString, NoteDto::class.java)
        return noteDtoResponse
    }

    protected fun createUser(): String {
        return UUID.randomUUID().toString()
    }
    protected fun createPermission(
        authToken: String,
        userId: String,
        noteId: Long,
        permission: Permission
    ) {
        mockIsUserExist(userId, true)
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/user/permissions")
                .header(AUTHORIZATION, "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "user_id" to userId,
                            "note_id" to noteId,
                            "permission" to permission
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isCreated)
    }

    protected fun mockIsUserExist(userId: String, isExist: Boolean) {
        mockSecurityService.stubFor(
            WireMock.get("/auth/is-user-exist/$userId")
                //                .withHeader("Authorization", equalTo("Bearer $authToken"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(isExist.toString())
                        .withHeader("Content-Type", "application/json")
                )
        )
    }

    protected fun getNoteById(token: String, noteId: Long): NoteDto {
        val contentAsString = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/note/$noteId")
                .header(AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        return mapper.readValue(contentAsString, NoteDto::class.java)
    }

    protected fun signInAs(username: String, role: String): String {
        return jwtUtil.generateToken(mapOf("role" to role), object : UserDetails {
            override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
                return mutableListOf(SimpleGrantedAuthority(role))
            }

            override fun getPassword(): String {
                return "password"
            }

            override fun getUsername(): String {
                return username
            }

        })

    }

    protected fun signInAs(username: String): String {
        return signInAs(username, "ROLE_USER")
    }
    protected fun signInAsAdmin(username: String): String {
        return signInAs(username, "ROLE_ADMIN")
    }
}