package org.example.notion.app.paragraph.controller

import jakarta.validation.Valid
import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.service.ParagraphService
import org.example.notion.sse.Message
import org.example.notion.sse.SseService
import org.example.notion.sse.Type
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("api/v1/paragraph")
class ParagraphController(
    private val paragraphService: ParagraphService,
    private val sseService: SseService
) {

    companion object {
        private const val MAX_PAGE_SIZE = 50L
    }

    @PostMapping("/create")
    fun createParagraph(
        @Valid @ModelAttribute paragraphCreateRequest: ParagraphCreateRequest
    ): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity(paragraphService.createParagraph(paragraphCreateRequest), HttpStatus.CREATED)
    }

    @GetMapping("/execute/{paragraphId}")
    fun executeParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long
    ): CompletableFuture<ResponseEntity<String>> {
        return paragraphService.executeParagraph(paragraphId)
            .thenApply { result ->
                sseService.sendMessage(2, Message(Type.PARAGRAPH_EXECUTED, result))
                ResponseEntity.ok(result)
            }
    }

    @DeleteMapping("/delete/{paragraphId}")
    fun deleteParagraph(
        @RequestHeader("user-id") userId: Long,
        @PathVariable paragraphId: Long
    ): ResponseEntity<Unit> {
        paragraphService.deleteParagraph(paragraphId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/get/{paragraphId}")
    fun getParagraph(
        @PathVariable paragraphId: Long
    ): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity.ok(paragraphService.getParagraph(paragraphId))
    }

    @PutMapping("/update")
    fun updateParagraph(
        @Valid @ModelAttribute paragraphUpdateRequest: ParagraphUpdateRequest
    ): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity.ok(paragraphService.updateParagraph(paragraphUpdateRequest))
    }

    @PostMapping("/position")
    fun changeParagraphPosition(
        @Valid @RequestBody changeParagraphPositionRequest: ChangeParagraphPositionRequest
    ): ResponseEntity<Unit> {
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
}