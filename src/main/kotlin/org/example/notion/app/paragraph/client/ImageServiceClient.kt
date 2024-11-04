package org.example.notion.app.paragraph.client

import com.google.common.net.HttpHeaders.AUTHORIZATION
import org.example.notion.app.paragraph.client.fallbacks.factories.ImageServiceFallbackFactory
import org.example.notion.app.paragraph.dto.GetImageResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "imageServiceClient",
    url = "\${integrations.image.url}",
    fallbackFactory = ImageServiceFallbackFactory::class
)
@Qualifier("imageServiceClient")
interface ImageServiceClient {

    @DeleteMapping("/api/v1/image/deleteByParagraphId/{paragraphId}")
    fun deleteByParagraphId(
        @PathVariable paragraphId: Long,
        @RequestHeader(AUTHORIZATION) token: String
    ): ResponseEntity<Unit>

    @DeleteMapping("/api/v1/image/deleteByName/{imageName}")
    fun deleteImageByName(
        @PathVariable imageName: String,
        @RequestHeader(AUTHORIZATION) token: String
    ): ResponseEntity<Unit>

    @GetMapping("/api/v1/image/get/{paragraphId}")
    fun getImageByParagraphId(
        @PathVariable paragraphId: String,
        @RequestHeader(AUTHORIZATION) token: String
    ): ResponseEntity<GetImageResponse>
}