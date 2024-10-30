package org.example.notion.configuration

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

@TestConfiguration
@ActiveProfiles("test")
class WireMockConfig {

    @Bean(name = ["mockCodeExecService"], initMethod = "start", destroyMethod = "stop")
    fun mockCodeExecService(): WireMockServer {
        return WireMockServer(80)
    }

    @Bean(name = ["mockImageService"], initMethod = "start", destroyMethod = "stop")
    fun mockImageService(): WireMockServer {
        return WireMockServer(81)
    }
}
