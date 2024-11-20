package org.example.notion.app.paragraph.client.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "notion-code-exec",
)
interface ExecutorFeignServiceClient {

    @GetMapping("/api/v1/execution/execute")
    fun getExecute(
        @RequestParam paragraphId: Long,
        @RequestParam code: String
    ): ResponseEntity<String>
}