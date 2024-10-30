package org.example.notion.app.paragraph.client.fallbacks

import org.example.notion.app.paragraph.client.ImageServiceClient
import org.example.notion.app.paragraph.dto.GetImageResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ImageServiceFallback: ImageServiceClient {

    override fun deleteByParagraphId(paragraphId: Long): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }

    override fun deleteImageByName(imageName: String): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }

    override fun getImageByParagraphId(paragraphId: String): ResponseEntity<GetImageResponse> {
        return ResponseEntity.ok(GetImageResponse(listOf("fallback")))
    }
}