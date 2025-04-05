import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.0.2"
    kotlin("plugin.serialization") version "1.9.25" apply false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

allprojects {
    repositories {
        mavenCentral()
        mavenCodeArtifact()
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        implementation("org.springframework.boot:spring-boot-starter-web")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("io.mockk:mockk:1.12.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        implementation("io.jsonwebtoken:jjwt-api:0.12.6")
        implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
        implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
        implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.2.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
        implementation("org.flywaydb:flyway-mysql")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("com.wafflestudio.spring:spring-boot-starter-waffle-secret-manager:1.0.1")
        implementation("software.amazon.awssdk:secretsmanager")
        implementation("software.amazon.awssdk:sts")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            mavenBom("software.amazon.awssdk:bom:2.25.70")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

fun RepositoryHandler.mavenCodeArtifact() {
    maven {
        val authToken =
            properties["codeArtifactAuthToken"] as String? ?: ByteArrayOutputStream().use {
                runCatching {
                    exec {
                        commandLine =
                            listOf(
                                "aws", "codeartifact", "get-authorization-token",
                                "--domain", "wafflestudio", "--domain-owner", "405906814034",
                                "--query", "authorizationToken", "--region", "ap-northeast-1", "--output", "text",
                            )
                        standardOutput = it
                    }
                }
                it.toString()
            }
        url = uri("https://wafflestudio-405906814034.d.codeartifact.ap-northeast-1.amazonaws.com/maven/spring-waffle/")
        credentials {
            username = "aws"
            password = authToken
        }
    }
}
