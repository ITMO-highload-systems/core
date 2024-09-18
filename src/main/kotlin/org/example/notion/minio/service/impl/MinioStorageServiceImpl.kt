package org.example.notion.minio.service.impl

import io.minio.*
import org.example.notion.minio.service.MinioStorageService
import org.example.notion.minio.util.calculateFileHash
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
class MinioStorageServiceImpl(
    private val minioClient: MinioClient,
) : MinioStorageService {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Value("\${minio.bucket}")
    lateinit var bucketName: String

    override fun uploadImg(file: MultipartFile) {
        val fileSize = file.size
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`("${file.calculateFileHash()}.jpg")
                .stream(file.inputStream, fileSize, -1)
                .contentType(file.contentType ?: "application/octet-stream")
                .build()
        )

        logger.info("File '${file.originalFilename}' uploaded to bucket '$bucketName'")
    }

    override fun getImage(fileHash: String): InputStream {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(fileHash)
                .build()
        )
    }
}