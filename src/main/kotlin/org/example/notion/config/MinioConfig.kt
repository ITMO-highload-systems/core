package org.example.notion.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig {

    @Value("\${minio.endpoint}")
    lateinit var endpoint: String

    @Value("\${minio.accessKey}")
    lateinit var accessKey: String

    @Value("\${minio.secretKey}")
    lateinit var secretKey: String

    @Value("\${minio.bucket}")
    lateinit var bucketName: String

    @Bean
    fun minioClient(): MinioClient {
        val minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
        return minioClient
    }
}