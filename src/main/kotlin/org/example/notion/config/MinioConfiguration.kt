package org.example.notion.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfiguration {

    @Bean
    fun minioClient(minioConnectionDetails: MinioConnectionDetails): MinioClient {
        val minioClient = MinioClient.builder()
            .endpoint(minioConnectionDetails.url)
            .credentials(minioConnectionDetails.accessKey, minioConnectionDetails.secretKey)
            .build()

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConnectionDetails.bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConnectionDetails.bucket).build())
        }
        return minioClient
    }

    @Bean
    @ConditionalOnMissingBean(MinioConnectionDetails::class)
    fun s3ConnectionDetails(minioProperties: MinioProperties): MinioConnectionDetails {
        return MinioConnectionDetailsImpl(minioProperties)
    }

    private class MinioConnectionDetailsImpl(val minioProperties: MinioProperties) : MinioConnectionDetails {
        override val url: String
            get() = minioProperties.endpoint

        override val accessKey: String
            get() = minioProperties.accessKey

        override val secretKey: String
            get() = minioProperties.secretKey

        override val bucket: String
            get() = minioProperties.bucket
    }
}