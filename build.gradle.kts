import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.7.10"
val hopliteVersion = "2.4.0"

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "io.arcane-solutions"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.14.2")
    implementation("io.projectreactor.netty:reactor-netty:1.0.21")
    implementation("io.projectreactor.rabbitmq:reactor-rabbitmq:1.5.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.7")

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    //config
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-json:$hopliteVersion")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    // testing
    testImplementation("io.projectreactor:reactor-test:3.4.21")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict -opt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
    }
}