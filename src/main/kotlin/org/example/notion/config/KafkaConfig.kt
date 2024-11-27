package org.example.notion.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {

    @Bean
    fun topic1() =
        TopicBuilder.name("events")
            .partitions(2)
            .replicas(3)
            .build()
}