package org.example.notion.app.paragraph.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${integrations.image.url}")
    private lateinit var imageServiceUrl: String

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(imageServiceUrl)
            .build()
    }
}