note-eureka-service-registry http://localhost:8761

notion-code-exec http://localhost:8083

notion-core http://localhost:8080

notion-s3 http://localhost:8082

notion-security http://localhost:8085


to add eureka client

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka


implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}
