package org.example.notion

import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.example.notion.minio.service.MinioStorageService
import org.example.notion.minio.service.impl.MinioStorageServiceImpl
import org.example.notion.minio.util.calculateFileHash
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

@ActiveProfiles("test")
class MinioContainerTest {

    private var minioStorageService: MinioStorageService = MinioStorageServiceImpl(MinioClient.builder()
        .endpoint("http://${minioContainer.host}:${minioContainer.getMappedPort(9000)}")
        .credentials("minioadmin", "minioadmin")
        .build())

    companion object {
        private val minioContainer = MinIOContainer("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data")
            .withExposedPorts(9000)
            .waitingFor(Wait.forHttp("/minio/health/live").forStatusCode(200))
        private lateinit var minioClient: MinioClient

        @BeforeAll
        @JvmStatic
        fun setup() {
            minioContainer.start()

            minioClient = MinioClient.builder()
                .endpoint("http://${minioContainer.host}:${minioContainer.getMappedPort(9000)}")
                .credentials("minioadmin", "minioadmin")
                .build()

            minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build())
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            minioContainer.stop()
        }
    }

    @Test
    fun `test container healthcheck`() {
        val url = URL("http://${minioContainer.host}:${minioContainer.getMappedPort(9000)}/minio/health/live")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        Assertions.assertEquals(200, responseCode, "MinIO container is not healthy")
    }

    @Test
    fun `test upload file`() {
        val bucketNameField = MinioStorageServiceImpl::class.java.getDeclaredField("bucketName")
        bucketNameField.isAccessible = true
        bucketNameField.set(minioStorageService, "test-bucket")
        val file = File("src/test/resources/images/Cat.jpg")
        val inputStream = FileInputStream(file)
        val multipartFile = MockMultipartFile(file.name, file.name, "image/jpeg", inputStream)
        val fileHash = multipartFile.calculateFileHash()
        minioStorageService.uploadImg(multipartFile)

        val imgResultInputStream = minioStorageService.getImage("$fileHash.jpg")

        val downloadedBytes = imgResultInputStream.readBytes()

        imgResultInputStream.close()

        val originalBytes = multipartFile.bytes
        Assertions.assertArrayEquals(originalBytes, downloadedBytes, "Содержимое загруженного и скачанного файла не совпадает!")
    }
}
