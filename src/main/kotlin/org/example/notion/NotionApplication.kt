package org.example.notion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import reactivefeign.spring.config.EnableReactiveFeignClients

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableFeignClients
@EnableReactiveFeignClients
@EnableHystrix
class NotionApplication

fun main(args: Array<String>) {
    runApplication<NotionApplication>(*args)
}
