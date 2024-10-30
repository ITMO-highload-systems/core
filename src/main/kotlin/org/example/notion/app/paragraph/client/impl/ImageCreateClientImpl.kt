package org.example.notion.app.paragraph.client.impl

import org.example.notion.app.paragraph.client.ImageCreateClient
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component
class ImageCreateClientImpl (
    private val webClient: WebClient
) : ImageCreateClient {


    override fun createImage(paragraphId: Long, filePart: FilePart): ResponseEntity<Unit> {
        val multipartBodyBuilder = MultipartBodyBuilder().apply {
            part("file", filePart)
        }

        return webClient.put()
            .uri("/api/v1/image/create/{paragraphId}", paragraphId)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .retrieve()
            .toEntity(Unit::class.java)
            .block()!!
    }


}