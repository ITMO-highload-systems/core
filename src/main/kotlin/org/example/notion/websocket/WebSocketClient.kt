package org.example.notion.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.example.notion.app.paragraph.dto.ExecuteParagraphRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.naming.ServiceUnavailableException

@Component
class WebSocketClient(
    private val stompClient: WebSocketStompClient,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(WebSocketClient::class.java)
    }

    private var session: StompSession? = null
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    @Value("\${websocket.server-address}")
    private var url: String? = null

    private val handler = CustomStompSessionHandler()

    @PostConstruct
    fun init() {
        connectWithRetries()
        Runtime.getRuntime().addShutdownHook(Thread {
            session?.disconnect()
        })
    }

    private fun connectWithRetries() {
        scheduler.scheduleAtFixedRate({
            try {
                if (session == null || !session!!.isConnected) {
                    logger.info("Attempting to connect to WebSocket...")
                    session = stompClient.connect(url!!, handler).get(5, TimeUnit.SECONDS)
                    logger.info("WebSocket connection established.")
                }
            } catch (ex: Exception) {
                logger.warn("Failed to connect to WebSocket: ${ex.message}")
            }
        }, 0, 10, TimeUnit.SECONDS)
    }


    fun sendMessage(destination: String, executeParagraphRequest: ExecuteParagraphRequest) {
        try {
            if (session == null) throw ServiceUnavailableException()
            val jsonPayload = objectMapper.writeValueAsString(executeParagraphRequest)
            session!!.send(destination, jsonPayload.toByteArray())
            logger.info("Sent: $executeParagraphRequest")
        } catch (ex: Exception) {
            logger.error("Failed to send message: ${ex.message}")
            throw ServiceUnavailableException("Code exec service is unavailable")
        }
    }
}