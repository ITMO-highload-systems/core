package org.example.notion.app.paragraph.controller

import jakarta.validation.Valid
import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.service.ParagraphService
import org.example.notion.app.user.UserContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("api/v1/paragraph")
class ParagraphController(
    private val paragraphService: ParagraphService
) {

    companion object {
        private const val MAX_PAGE_SIZE = 50L
    }

    @PostMapping("/create")
    fun createParagraph(
        @RequestHeader("user-id") userId: Long,
        @Valid @RequestBody paragraphCreateRequest: ParagraphCreateRequest
    ): ResponseEntity<ParagraphGetResponse> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity(paragraphService.createParagraph(paragraphCreateRequest), HttpStatus.CREATED)
    }

    @GetMapping("/execute/{paragraphId}")
    fun executeParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long
    ): ResponseEntity<String> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(paragraphService.executeParagraph(paragraphId))
    }

    @DeleteMapping("/delete/{paragraphId}")
    fun deleteParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long
    ): ResponseEntity<Unit> {
        UserContext.setCurrentUser(userId)
        paragraphService.deleteParagraph(paragraphId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/get/{paragraphId}")
    fun getParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long
    ): ResponseEntity<ParagraphGetResponse> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(paragraphService.getParagraph(paragraphId))
    }

    @PutMapping("/update")
    fun updateParagraph(
        @Valid @RequestHeader("user-id") userId: Long,
        @Valid @RequestBody paragraphUpdateRequest: ParagraphUpdateRequest
    ): ResponseEntity<ParagraphGetResponse> {
        UserContext.setCurrentUser(userId)
        return ResponseEntity.ok(paragraphService.updateParagraph(paragraphUpdateRequest))
    }

    @PostMapping("/position")
    fun changeParagraphPosition(
        @Valid @RequestHeader("user-id") userId: Long,
        @Valid @RequestBody changeParagraphPositionRequest: ChangeParagraphPositionRequest
    ): ResponseEntity<Unit> {
        UserContext.setCurrentUser(userId)
        paragraphService.changeParagraphPosition(changeParagraphPositionRequest)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/all")
    fun getAllParagraphs(
        @Valid @RequestParam("page", defaultValue = "0") page: Long,
        @Valid @RequestParam("pageSize", defaultValue = "50") pageSize: Long,
    ): ResponseEntity<List<ParagraphGetResponse>> {
        val limitedPageSize = pageSize.coerceAtMost(MAX_PAGE_SIZE)
        val paragraphs = paragraphService.findAllParagraphs(limitedPageSize, page)
        val headers = HttpHeaders()
        headers.add("X-Total-Count", paragraphs.size.toString())
        return ResponseEntity.ok().headers(headers).body(paragraphs)
    }

    @PutMapping("/addImageToParagraph/{paragraphId}")
    fun addImageToParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long,
        @RequestPart("file") filePart: FilePart
    ): ResponseEntity<Unit> {
        UserContext.setCurrentUser(userId)
        paragraphService.addImageToParagraph(paragraphId, filePart)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @DeleteMapping("/deleteImageFromParagraph/{paragraphId}")
    fun deleteImageFromParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long,
        @RequestParam imageName: String
    ): ResponseEntity<Unit> {
        UserContext.setCurrentUser(userId)
        paragraphService.deleteImageFromParagraph(paragraphId, imageName)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/create/{paragraphId}")
    fun saveImage(
        @PathVariable paragraphId: Long,
        @RequestPart("file") filePart: FilePart
    ): Mono<ResponseEntity<Unit>> {
        return Mono.just(ResponseEntity(HttpStatus.CREATED))
    }
}