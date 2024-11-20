package org.example.notion.app.paragraph.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.example.notion.app.paragraph.client.feign.ExecutorFeignServiceClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ExecutorServiceClient(
    private val executorFeignServiceClient: ExecutorFeignServiceClient
) {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(ExecutorServiceClient::class.java)
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getExecuteFallback")
    fun getExecute(paragraphId: Long, code: String): ResponseEntity<String> {
        return executorFeignServiceClient.getExecute(paragraphId, code)
    }

    fun getExecuteFallback(paragraphId: Long, code: String, ex: Throwable): ResponseEntity<String> {
        logger.error("Fallback for getExecute invoked due to: ${ex.message}")
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is unavailable")
    }
}