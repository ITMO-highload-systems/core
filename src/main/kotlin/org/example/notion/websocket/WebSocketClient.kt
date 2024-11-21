package org.example.notion.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.example.notion.app.paragraph.dto.ExecuteParagraphRequest
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.WebSocketStompClient

@Component
class WebSocketClient(
    private val stompClient: WebSocketStompClient,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(WebSocketClient::class.java)
    }

    private lateinit var session: StompSession

    private val url = "ws://localhost:52835/ws"

    private val handler = CustomStompSessionHandler()

    @PostConstruct
    fun init() {
        session = stompClient.connect(url, handler).get()
        Runtime.getRuntime().addShutdownHook(Thread {
            session.disconnect()
        })
    }


    fun sendMessage(destination: String, executeParagraphRequest: ExecuteParagraphRequest) {
        val jsonPayload = objectMapper.writeValueAsString(executeParagraphRequest)
        session.send(destination, jsonPayload.toByteArray())
        logger.info("Sent: $executeParagraphRequest")
    }
}