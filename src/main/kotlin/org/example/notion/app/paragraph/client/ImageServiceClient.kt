package org.example.notion.app.paragraph.client

import org.example.notion.app.paragraph.client.fallbacks.factories.ImageServiceFallbackFactory
import org.example.notion.app.paragraph.dto.GetImageResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "notion-s3",
    fallbackFactory = ImageServiceFallbackFactory::class
)
interface ImageServiceClient {

    @DeleteMapping("/api/v1/image/by-paragraph/{paragraphId}")
    fun deleteByParagraphId(
        @PathVariable paragraphId: Long
    ): ResponseEntity<Unit>

    @DeleteMapping("/api/v1/image/by-name/{imageName}")
    fun deleteImageByName(
        @PathVariable imageName: String
    ): ResponseEntity<Unit>

    @GetMapping("/api/v1/image/{paragraphId}")
    fun getImageByParagraphId(
        @PathVariable paragraphId: String
    ): ResponseEntity<GetImageResponse>
}