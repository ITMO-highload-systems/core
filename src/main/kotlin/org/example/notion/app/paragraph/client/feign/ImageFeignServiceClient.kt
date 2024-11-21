package org.example.notion.app.paragraph.client.feign

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.example.notion.app.paragraph.dto.GetImageResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "notion-s3"
)
interface ImageFeignServiceClient {

    @DeleteMapping("/api/v1/image/by-paragraph/{paragraphId}")
    @CircuitBreaker(name = "default")
    fun deleteByParagraphId(
        @PathVariable paragraphId: Long
    ): ResponseEntity<Unit>

    @DeleteMapping("/api/v1/image/by-name/{imageName}")
    @CircuitBreaker(name = "default")
    fun deleteImageByName(
        @PathVariable imageName: String
    ): ResponseEntity<Unit>

    @GetMapping("/api/v1/image/{paragraphId}")
    @CircuitBreaker(name = "default")
    fun getImageByParagraphId(
        @PathVariable paragraphId: String
    ): ResponseEntity<GetImageResponse>
}