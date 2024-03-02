import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "ru.chimchima"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass.set("ru.chimchima.ChimberBotKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.lavalink.dev/snapshots") // For 727959e9f621fc457b3a5adafcfffb55fdeaa538 fix
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    implementation("dev.kord:kord-core:0.13.1")
    implementation("dev.kord:kord-core-voice:0.13.1")

//    implementation("dev.arbjerg:lavaplayer:2.1.0")
    implementation("dev.arbjerg:lavaplayer:727959e9f621fc457b3a5adafcfffb55fdeaa538-SNAPSHOT")
    implementation("io.ktor:ktor-client-core:2.3.8")
    implementation("org.slf4j:slf4j-simple:2.0.12")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn"))
        jvmTarget.set(JVM_17)
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ru.chimchima.ChimberBotKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
