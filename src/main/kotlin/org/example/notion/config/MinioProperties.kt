package org.example.notion.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("minio")
@JvmRecord
data class MinioProperties(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String
)