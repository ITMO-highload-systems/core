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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController("api/paragraph")
class ParagraphController(
    private val paragraphService: ParagraphService,
    private val sseService: SseService
) {

    @PostMapping("/create")
    fun createParagraph(@Valid @RequestBody paragraphCreateRequest: ParagraphCreateRequest) {
        paragraphService.createParagraph(paragraphCreateRequest)
    }

    @PostMapping("/execute/{paragraphId}")
    fun executeParagraph(@PathVariable paragraphId: Long): CompletableFuture<ResponseEntity<String>> {
        return paragraphService.executeParagraph(paragraphId)
            .thenApply { result ->
                sseService.sendMessage(2, Message(Type.PARAGRAPH_EXECUTED, result))
                ResponseEntity.ok(result)
            }
    }

    @DeleteMapping("/delete/{paragraphId}")
    fun deleteParagraph(@PathVariable paragraphId: Long) {
        paragraphService.deleteParagraph(paragraphId)
    }

    @GetMapping("/get/{paragraphId}")
    fun getParagraph(@PathVariable paragraphId: Long): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity.ok(paragraphService.getParagraph(paragraphId))
    }

    @PostMapping("/update")
    fun updateParagraph(@Valid @RequestBody paragraphUpdateRequest: ParagraphUpdateRequest): ResponseEntity<ParagraphGetResponse> {
        return ResponseEntity.ok(paragraphService.updateParagraph(paragraphUpdateRequest))
    }

    @PostMapping("/position")
    fun changeParagraphPosition(@Valid @RequestBody changeParagraphPositionRequest: ChangeParagraphPositionRequest) {
        paragraphService.changeParagraphPosition(changeParagraphPositionRequest)
    }
}