package org.example.notion.app.paragraph.client.fallbacks.factories

import org.example.notion.app.paragraph.client.ImageServiceClient
import org.example.notion.app.paragraph.client.fallbacks.ImageServiceFallback
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class ImageServiceFallbackFactory(
    private val feignFallback: ImageServiceFallback
): FallbackFactory<ImageServiceClient> {

    override fun create(cause: Throwable?): ImageServiceClient {
        return feignFallback
    }
}