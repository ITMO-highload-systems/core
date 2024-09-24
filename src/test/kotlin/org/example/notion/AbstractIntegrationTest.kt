package org.example.notion

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.mock.web.MockAsyncContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc
    var objectMapper: ObjectMapper = jacksonObjectMapper()

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

    fun subscribe(noteId: Long): MvcResult {
        return mockMvc.perform(MockMvcRequestBuilders.get("/sse/bind/$noteId/user"))
            .andExpect(MockMvcResultMatchers.request().asyncStarted()).andReturn()
    }

    fun sendSse(noteId: Long, message: Map<String, Any>) {
        val valueAsBytes = objectMapper.writeValueAsBytes(message)
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

}