package org.example.notion.minio

import org.example.notion.AbstractIntegrationTest
import org.example.notion.minio.service.impl.MinioStorageServiceImpl
import org.example.notion.minio.util.calculateFileHash
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URI

class MinioContainerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var minioStorageService: MinioStorageServiceImpl

    @Test
    fun `test container healthcheck`() {
        val url =
            URI.create("http://${minio.host}:${minio.getMappedPort(9000)}/minio/health/live").toURL()

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        Assertions.assertEquals(200, responseCode, "MinIO container is not healthy")
    }

    @Test
    fun `test upload file`() {
        val file = File("src/test/resources/images/Cat.jpg")
        val inputStream = FileInputStream(file)
        val multipartFile = MockMultipartFile(file.name, file.name, "image/jpeg", inputStream)
        val fileHash = multipartFile.calculateFileHash()
        minioStorageService.uploadImg(multipartFile)

        val imgResultInputStream = minioStorageService.getImageContent("$fileHash.jpg")

        val downloadedBytes = imgResultInputStream.readBytes()

        imgResultInputStream.close()

        val originalBytes = multipartFile.bytes
        Assertions.assertArrayEquals(originalBytes, downloadedBytes, "Содержимое загруженного и скачанного файла не совпадает!")
    }
}