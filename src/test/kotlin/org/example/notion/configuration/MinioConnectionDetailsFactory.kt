package org.example.notion.configuration

import org.example.notion.config.MinioConnectionDetails
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource
import org.testcontainers.containers.MinIOContainer

open class MinioConnectionDetailsFactory : ContainerConnectionDetailsFactory<MinIOContainer?, MinioConnectionDetails>() {
    override fun getContainerConnectionDetails(source: ContainerConnectionSource<MinIOContainer?>): MinioConnectionDetails {
        return MinioContainerConnectionDetails(source)
    }

    protected class MinioContainerConnectionDetails(source: ContainerConnectionSource<MinIOContainer?>) :
        ContainerConnectionDetails<MinIOContainer?>(source), MinioConnectionDetails {
        override val url: String?
            get() = container!!.s3URL
        override val accessKey: String?
            get() = container!!.userName
        override val secretKey: String?
            get() = container!!.password
        override val bucket: String?
            get() = "bucket-1"

    }
}