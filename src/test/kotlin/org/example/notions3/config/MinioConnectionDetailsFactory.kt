package org.example.notions3.config

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource
import org.testcontainers.containers.MinIOContainer

class MinioConnectionDetailsFactory : ContainerConnectionDetailsFactory<MinIOContainer, MinioConnectionDetails>() {
    override fun getContainerConnectionDetails(source: ContainerConnectionSource<MinIOContainer>): MinioConnectionDetails {
        return MinioContainerConnectionDetails(source)
    }

    private class MinioContainerConnectionDetails(source: ContainerConnectionSource<MinIOContainer>) :
        ContainerConnectionDetails<MinIOContainer>(source), MinioConnectionDetails {
        override val url: String
            get() = container.s3URL
        override val accessKey: String
            get() = container.userName
        override val secretKey: String
            get() = container.password
        override val bucket: String
            get() = "bucket-1"

    }
}