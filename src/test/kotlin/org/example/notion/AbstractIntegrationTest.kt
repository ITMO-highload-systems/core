package org.example.notion

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

@ActiveProfiles("test")
@SpringBootTest(classes = [NotionApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {

    companion object {
        @ServiceConnection
        internal var minio = MinIOContainer("minio/minio:latest").apply {
            withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200))
        }

        @ServiceConnection
        internal var postgres = PostgreSQLContainer("postgres:16-alpine")


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
}
