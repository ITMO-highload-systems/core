package org.example.notion

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.user.dto.UserResponseDto
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    companion object {
        @ServiceConnection
        internal var minio = MinIOContainer("minio/minio:latest").apply {
            withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200))
        }

        @ServiceConnection
        internal var postgres = PostgreSQLContainer("postgres:latest")


        @BeforeAll
        @JvmStatic
        fun setup() {
            minio.start()
            postgres.start()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            minio.stop()
            postgres.stop()
        }
    }

    protected fun createNote(userId: Long): NoteDto {
        val noteString = mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/note")
                .header("user-id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapOf("title" to "title", "description" to "description")))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString

        val noteDtoResponse: NoteDto = mapper.readValue(noteString, NoteDto::class.java)
        return noteDtoResponse
    }

    protected fun createUser(): UserResponseDto {
        val userString = mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        mapOf(
                            "email" to UUID.randomUUID().toString() + "email.com",
                            "password" to UUID.randomUUID().toString()
                        )
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString

        val userDtoResponse: UserResponseDto = mapper.readValue(userString, UserResponseDto::class.java)
        return userDtoResponse
    }
}