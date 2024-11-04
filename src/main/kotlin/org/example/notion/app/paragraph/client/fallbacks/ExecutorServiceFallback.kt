package org.example.notion.app.paragraph.client.fallbacks

import org.example.notion.app.paragraph.client.ExecutorServiceClient
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ExecutorServiceFallback : ExecutorServiceClient {

    override fun getExecute(paragraphId: Long, code: String, token: String): ResponseEntity<String> {
        return ResponseEntity.ok("Fallback response due to service unavailability")
    }
}