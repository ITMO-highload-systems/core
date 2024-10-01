package org.example.notion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableAsync
class NotionApplication

fun main(args: Array<String>) {
    runApplication<NotionApplication>(*args)
}
