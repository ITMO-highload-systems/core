package org.example.notion.config

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION

@Configuration
class FeignConfig {

    @Bean
    fun interceptor(jwtUtil: JwtUtil): ClientInterceptor {
        return ClientInterceptor(jwtUtil)
    }

    class ClientInterceptor(jwtUtil: JwtUtil) : RequestInterceptor {
        private var token = jwtUtil.generateServerToken()

        override fun apply(template: RequestTemplate) {
            template.header(AUTHORIZATION, "Bearer $token")
        }
    }
}
