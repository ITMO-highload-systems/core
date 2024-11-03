package org.example.notion.app.paragraph.client.fallbacks.factories

import org.example.notion.app.paragraph.client.ExecutorServiceClient
import org.example.notion.app.paragraph.client.fallbacks.ExecutorServiceFallback
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component


@Component
class ExecutorServiceFallbackFactory(
    private val feignFallback: ExecutorServiceFallback
): FallbackFactory<ExecutorServiceClient> {

    override fun create(cause: Throwable?): ExecutorServiceClient {
        return feignFallback
    }
}