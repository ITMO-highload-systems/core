package org.example.notion.app.paragraph.controller

import jakarta.validation.Valid
import org.example.notion.app.paragraph.dto.ChangeParagraphPositionRequest
import org.example.notion.app.paragraph.dto.ParagraphCreateRequest
import org.example.notion.app.paragraph.dto.ParagraphGetResponse
import org.example.notion.app.paragraph.dto.ParagraphUpdateRequest
import org.example.notion.app.paragraph.service.ParagraphService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/paragraph")
class ParagraphController(
    private val paragraphService: ParagraphService
) {

    companion object {
        private const val MAX_PAGE_SIZE = 50L
    }

    @PostMapping
    fun createParagraph(
        @Valid @RequestBody paragraphCreateRequest: ParagraphCreateRequest
    ): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity(paragraphService.createParagraph(paragraphCreateRequest), HttpStatus.CREATED)
    }

    @GetMapping("/execute/{paragraphId}")
    fun executeParagraph(
        @PathVariable paragraphId: Long
    ): ResponseEntity<String> {
        return ResponseEntity.ok(paragraphService.executeParagraph(paragraphId))
    }

    @DeleteMapping("/{paragraphId}")
    fun deleteParagraph(
        @PathVariable paragraphId: Long
    ): ResponseEntity<Unit> {
        paragraphService.deleteParagraph(paragraphId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{paragraphId}")
    fun getParagraph(
        @PathVariable paragraphId: Long
    ): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity.ok(paragraphService.getParagraph(paragraphId))
    }

    @PutMapping
    fun updateParagraph(
        @Valid @RequestBody paragraphUpdateRequest: ParagraphUpdateRequest
    ): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity.ok(paragraphService.updateParagraph(paragraphUpdateRequest))
    }

    @PutMapping("/position")
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

    @GetMapping("/isPossibleAddImageToParagraph/{paragraphId}")
    fun isPossibleAddImageToParagraph(
        @PathVariable paragraphId: Long
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(paragraphService.isPosssibleAddImageToParagraph(paragraphId))
    }

    @DeleteMapping("/deleteImageFromParagraph/{paragraphId}")
    fun deleteImageFromParagraph(
        @PathVariable paragraphId: Long,
        @RequestParam imageName: String
    ): ResponseEntity<Unit> {
        paragraphService.deleteImageFromParagraph(paragraphId, imageName)
        return ResponseEntity.noContent().build()
    }
}