plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("kapt") version "1.9.10"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

// Dependencies
val testContainersVersion = "1.20.1"
val mapstructVersion = "1.6.0"
val flyWayVersion = "10.10.0"
val postgresqlVersion = "42.7.3"
val jacksonVersion = "2.17.2"
val kotlinJetBrainsVersion = "2.0.20"
val cloudVersion = "4.1.3"
val wiremockVersion = "3.9.2"
val jettyServer = "11.0.24"
val netflixVersion = "2.2.10.RELEASE"
val springDataJdbc = "3.3.5"
val jjwtApiVersion = "0.11.2"
val jjwtImplVersion = "0.11.5"
val jjwtJacksonVersion = "0.11.1"
val springSecurityTestVersion = "6.3.4"
val cloudFeignVersion = "1.4.7.RELEASE"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-hystrix:$netflixVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-hystrix-dashboard:$netflixVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-feign:$cloudFeignVersion")
    implementation("org.springframework.data:spring-data-jdbc:$springDataJdbc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.flywaydb:flyway-core:$flyWayVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtApiVersion")


    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtImplVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtJacksonVersion")

    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")



    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flyWayVersion")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test:$springSecurityTestVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinJetBrainsVersion")
    testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")

}
kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

// Compiler options for Kotlin
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// Test task configuration
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    withType<JavaCompile>
    {
        dependsOn(processResources)
    }
}