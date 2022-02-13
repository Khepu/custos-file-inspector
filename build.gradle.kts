import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.6.10"

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "io.arcane-solutions"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.14.1")
    implementation("io.projectreactor.netty:reactor-netty:1.0.15")
    implementation("io.projectreactor.rabbitmq:reactor-rabbitmq:1.5.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.5")

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    // testing
    testImplementation("io.projectreactor:reactor-test:3.4.14")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict -opt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
    }
}