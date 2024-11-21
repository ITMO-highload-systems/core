package org.example.notion.websocket

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter

class CustomStompSessionHandler : StompSessionHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(CustomStompSessionHandler::class.java)

    override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
        logger.info("Connected to WebSocket!")
    }

    override fun handleFrame(headers: StompHeaders, payload: Any?) {
        logger.info("Received: $payload")
    }
}