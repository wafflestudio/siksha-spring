plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("plugin.serialization")
}

val dl4jVersion = "1.0.0-M2.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.10")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37")
    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("org.deeplearning4j:deeplearning4j-core:$dl4jVersion")
    implementation("org.deeplearning4j:deeplearning4j-nlp:$dl4jVersion")
    implementation("org.nd4j:nd4j-native-platform:$dl4jVersion")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.register<JavaExec>("trainMenuWord2Vec") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("siksha.wafflestudio.core.domain.main.meal.usecase.TrainMenuWord2Vec")
    args(
        providers.gradleProperty("menuNormalizerInput").getOrElse("data/menu-normalizer-pairs.tsv"),
        providers.gradleProperty("menuNormalizerOutput").getOrElse("models/menu-word2vec.bin"),
    )
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<JavaCompile> { options.compilerArgs.add("-parameters") }
