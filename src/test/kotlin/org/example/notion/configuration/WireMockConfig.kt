package org.example.notion.configuration

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles

@TestConfiguration
@ActiveProfiles("test")
class WireMockConfig {
    @Value("\${mock-port.s3}")
    private var portS3: Int = 81

    @Value("\${mock-port.code-exec}")
    private var portExec: Int = 83

    @Value("\${mock-port.security}")
    private var portSecurity: Int = 82

    @Bean(name = ["mockCodeExecService"], initMethod = "start", destroyMethod = "stop")
    fun mockCodeExecService(): WireMockServer {
        return WireMockServer(portExec)
    }

    @Bean(name = ["mockImageService"], initMethod = "start", destroyMethod = "stop")
    fun mockImageService(): WireMockServer {
        return WireMockServer(portS3)
    }

    @Bean(name = ["mockSecurityService"], initMethod = "start", destroyMethod = "stop")
    fun mockSecurityService(): WireMockServer {
        return WireMockServer(portSecurity)
    }
}

