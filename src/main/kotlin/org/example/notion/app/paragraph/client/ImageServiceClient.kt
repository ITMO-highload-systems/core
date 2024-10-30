package org.example.notion.app.paragraph.client

import org.example.notion.app.paragraph.client.fallbacks.factories.ImageServiceFallbackFactory
import org.example.notion.app.paragraph.dto.GetImageResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "imageServiceClient",
    url = "\${integrations.image.url}",
    fallbackFactory = ImageServiceFallbackFactory::class
)
interface ImageServiceClient {

    @DeleteMapping("/deleteByParagraphId/{paragraphId}")
    fun deleteByParagraphId(
        @PathVariable paragraphId: Long
    ): ResponseEntity<Unit>

    @DeleteMapping("/deleteByName/{imageName}")
    fun deleteImageByName(
        @PathVariable imageName: String
    ): ResponseEntity<Unit>

    @GetMapping("/get/{paragraphId}")
    fun getImageByParagraphId(
        @PathVariable paragraphId: String
    ): ResponseEntity<GetImageResponse>
}