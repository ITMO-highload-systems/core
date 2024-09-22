package org.example.notion.sse

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.function.Consumer

@Service
class SseService {
    private val sseConnections: MutableMap<Long, MutableList<SseConnection>> = HashMap()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun openConnection(username: String, noteId: Long): SseConnection {
        val sseConnection = SseConnection(username, noteId, SseEmitter(-1L))
        sseConnections.computeIfAbsent(noteId) { k: Long? -> ArrayList() }.add(sseConnection)
        logger.info("Connection open for username {} and note_id {}", username, noteId)

        sseConnection.sseEmitter.onCompletion {
            logger.debug(
                "Released SSE connection {}",
                sseConnection
            )
        }

        return sseConnection
    }

    fun sendMessage(noteId: Long, payload: Message) {
        if (sseConnections.containsKey(noteId)) {
            sseConnections[noteId]!!.forEach(Consumer { sseConnection: SseConnection ->
                sseConnection.sendMessage(payload)
            })
        }
    }
}