package org.example.notion.app.paragraph.client

import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart

interface ImageCreateClient {

    fun createImage(paragraphId: Long, filePart: FilePart): ResponseEntity<Unit>
}