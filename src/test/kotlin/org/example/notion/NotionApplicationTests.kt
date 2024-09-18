package org.example.notion

import io.minio.MinioClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class NotionApplicationTests {

    @MockBean
    lateinit var minioClient: MinioClient
    @Test
    fun contextLoads() {
    }

}
