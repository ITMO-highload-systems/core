package org.example.notion.app.paragraph.client

import org.example.notion.app.paragraph.client.fallbacks.factories.ExecutorServiceFallbackFactory
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "EXECUTOR-SERVICE",
    fallbackFactory = ExecutorServiceFallbackFactory::class
)
interface ExecutorServiceClient {

    @GetMapping("/api/v1/execution/execute")
    fun getExecute(
        @RequestParam paragraphId: Long,
        @RequestParam code: String
    ): ResponseEntity<String>
}