package org.example.notion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class NotionApplication

fun main(args: Array<String>) {
    runApplication<NotionApplication>(*args)
}
