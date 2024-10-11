package org.example.notion

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.notion.app.note.dto.NoteDto
import org.example.notion.app.user.dto.UserResponseDto
import org.example.notion.app.userPermission.entity.Permission
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.mock.web.MockAsyncContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import ru.tinkoff.helicopter.core.ClockTestConfiguration.TestClockProxy
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        @ServiceConnection
        internal var minio = MinIOContainer("minio/minio:latest").apply {
            withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200))
        }

        @ServiceConnection
        internal var postgres = PostgreSQLContainer("postgres:latest")

        internal val genericContainer = GenericContainer(DockerImageName.parse("python:3.10-slim"))
            .withCommand("tail", "-f", "/dev/null")


        @BeforeAll
        @JvmStatic
        fun setup() {
            minio.start()
            postgres.start()
            genericContainer.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDockerProperties(registry: DynamicPropertyRegistry) {
            registry.add("docker.image") { genericContainer.containerName }
        }
    }

    @BeforeEach
    fun prepareEnv() {
        TestClockProxy.setToFixedClock()
    }

    fun subscribe(noteId: Long): MvcResult {
        return mockMvc.perform(MockMvcRequestBuilders.get("/sse/bind/$noteId/user"))
            .andExpect(MockMvcResultMatchers.request().asyncStarted()).andReturn()
    }

    fun sendSse(noteId: Long, message: Map<String, Any>) {
        val valueAsBytes = mapper.writeValueAsBytes(message)
        logger.debug("Sending JSON: ${String(valueAsBytes)}")
        mockMvc.perform(
            MockMvcRequestBuilders.post("/sse/send/$noteId").content(valueAsBytes)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun getSse(result: MvcResult): ResultActions {
        val execService = Executors.newScheduledThreadPool(1)
        val timeout = 500L
        val timeUnit = TimeUnit.MILLISECONDS
        val asyncContext = result.request.asyncContext as MockAsyncContext?
        execService.schedule({
            for (listener in asyncContext!!.listeners) try {
                listener.onTimeout(null)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }, timeout, timeUnit)

        result.asyncResult

        return mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))

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
    protected fun createPermission(
        ownerId: Long,
        userId: Long,
        noteId: Long,
        permission: Permission
    ) {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/user/permissions")
                .header("user-id", ownerId)
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

    protected fun getNoteById(userId: Long, noteId: Long): NoteDto {
        val contentAsString = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/note/$noteId").header("user-id", userId)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        return mapper.readValue(contentAsString, NoteDto::class.java)
    }
}