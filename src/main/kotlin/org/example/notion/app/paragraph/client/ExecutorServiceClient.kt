package org.example.notion.app.paragraph.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "notion-code-exec"
)
interface ExecutorServiceClient {

    @GetMapping("/api/v1/execution/execute")
    @CircuitBreaker(name = "default")
    fun getExecute(
        @RequestParam paragraphId: Long,
        @RequestParam code: String
    ): ResponseEntity<String>
}