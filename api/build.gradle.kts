import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(project(":core"))
}

tasks.withType<BootJar> {
    mainClass.set("siksha.wafflestudio.SikshaApplicationKt")
}
