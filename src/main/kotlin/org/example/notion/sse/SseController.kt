package org.example.notion.sse

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/sse")
class SseController(private val sseConnectionService: SseService) {

    @GetMapping(path = ["/bind/{noteId}/{username}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun bindUser(@PathVariable noteId: Long, @PathVariable username: String): SseEmitter {
        return sseConnectionService.openConnection(
            username,
            noteId
        ).sseEmitter
    }

    @PostMapping(path = ["/send/{noteId}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendMessage(@PathVariable noteId: Long, @RequestBody message: Message) {
        sseConnectionService.sendMessage(noteId, message)
    }
}