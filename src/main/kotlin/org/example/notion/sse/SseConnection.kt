package org.example.notion.sse

import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


data class SseConnection(val username: String, val noteId: Long, val sseEmitter: SseEmitter) {
    private val reconnectTime: Long = 400

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun sendMessage(payload: AbstractMessage) {
        logger.info("Send message {} to connection {}", payload, this)

        try {
            sseEmitter.send(
                SseEmitter.event()
                    .id(System.currentTimeMillis().toString())
                    .name("message")
                    .reconnectTime(reconnectTime)
                    .data(payload)
            )
        } catch (e: Throwable) {
            logger.warn("Unable to send SSE: {}", e.message, e)
        }
    }
}