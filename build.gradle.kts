import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    application
}

group = "ru.chimchima"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass = "ru.chimchima.ChimberBotKt"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.lavalink.dev/releases")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    implementation("dev.kord:kord-core:0.14.0")
    implementation("dev.kord:kord-core-voice:0.14.0")

    implementation("dev.arbjerg:lavaplayer:2.2.1")
    implementation("dev.lavalink.youtube:v2:1.5.2")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("org.slf4j:slf4j-simple:2.0.14")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ru.chimchima.ChimberBotKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
