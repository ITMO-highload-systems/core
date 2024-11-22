package org.example.notion.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class SseService(private val kafkaTemplate: KafkaTemplate<String, String>, private val objectMapper: ObjectMapper) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun sendMessage(payload: Message) {
        val send = kafkaTemplate.send(
            "events",
            objectMapper.writeValueAsString(payload)
        )
        send.whenComplete { result, ex ->
            if (ex == null) {
                logger.info(
                    "Sent message=[$payload] with offset=[${result.recordMetadata.offset()})]"
                )
            } else {
                logger.error(
                    "Unable to send message=[$payload] due to :  ${ex.message}"
                )
            }
        }
    }
}