package org.example.notion.service.impl

import io.minio.*
import lombok.extern.slf4j.Slf4j
import org.example.notion.service.MinioStorageService
import org.example.notion.util.calculateFileHash
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Slf4j
@Service
class MinioStorageServiceImpl(
    private val minioClient: MinioClient
): MinioStorageService {

    @Value("\${minio.bucket}")
    lateinit var bucketName: String

    override fun uploadImg(file: MultipartFile) {
        try {
            require(isValidImageFile(file)) { "Invalid file type. Only JPEG or JPG images are allowed." }
            createBucketIfNotExist(bucketName)

            val fileSize = file.size
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`("${calculateFileHash(file)}.jpg")
                    .stream(file.inputStream, fileSize, -1)
                    .contentType(file.contentType ?: "application/octet-stream")
                    .build()
            )

            println("File '${file.originalFilename}' uploaded to bucket '$bucketName'")
        } catch (e: Exception) {
            println("Error occurred: $e")
        }
    }

    override fun getImage(fileHash: String): InputStream {
        return try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileHash)
                    .build()
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Error occurred while fetching file with hash '$fileHash' from bucket '$bucketName': ${e.message}", e
            )
        }
    }

    private fun createBucketIfNotExist(bucketName: String) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
    }

    private fun isValidImageFile(file: MultipartFile): Boolean {
        val fileName = file.originalFilename?.lowercase()
        val mimeType = file.contentType?.lowercase()

        return (fileName?.endsWith(".jpg") == true || fileName?.endsWith(".jpeg") == true) &&
                (mimeType == "image/jpeg")
    }
}