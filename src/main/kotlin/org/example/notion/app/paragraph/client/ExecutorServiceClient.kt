package org.example.notion.app.paragraph.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.example.notion.app.paragraph.client.feign.ExecutorFeignServiceClient
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import javax.naming.ServiceUnavailableException

@Service
class ExecutorServiceClient(
    private val executorFeignServiceClient: ExecutorFeignServiceClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExecutorServiceClient::class.java)
        private const val FALLBACK_MESSAGE = "Executor Service is unavailable"
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getExecuteFallback")
    fun getExecute(paragraphId: Long, code: String): ResponseEntity<String> {
        return executorFeignServiceClient.getExecute(paragraphId, code)
    }

    fun getExecuteFallback(paragraphId: Long, code: String, ex: Throwable): ResponseEntity<String> {
        logger.error("Fallback for getExecute invoked due to: ${ex.message}")
        throw ServiceUnavailableException(FALLBACK_MESSAGE)
    }
}