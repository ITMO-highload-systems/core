package org.example.notion.sse

import org.example.notion.AbstractIntegrationTest
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


class SseTest : AbstractIntegrationTest() {

    @Test
    fun test() {
        val result = subscribe(1)
        val messageValue = "message"
        sendSse(1, mapOf("type" to Type.PARAGRAPH_CHANGED, "objectValue" to messageValue))
        getSse(result).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(messageValue)))
    }
}