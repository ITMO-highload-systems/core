plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("kapt") version "1.9.10"
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
val minioVersion = "8.5.11"
val testContainersVersion = "1.20.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.minio:minio:$minioVersion")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.42.1")
    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-jdbc
    implementation("org.springframework.data:spring-data-jdbc")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jdbc
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    //map
    implementation("org.mapstruct:mapstruct:1.6.0")
    kapt("org.mapstruct:mapstruct-processor:1.6.0")


    // Optional configuration processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Compile-time dependencies
    compileOnly("org.projectlombok:lombok")

    // Development dependencies
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Runtime dependencies
    runtimeOnly("org.postgresql:postgresql")

    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:minio:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
}
kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
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