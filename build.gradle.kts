import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
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
    maven("https://m2.dv8tion.net/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")

    implementation("io.ktor:ktor-client-core:2.2.2")
    implementation("org.slf4j:slf4j-simple:2.0.5")
    implementation("com.github.walkyst:lavaplayer-fork:1.3.99.2")
    implementation("dev.kord:kord-core:0.8.x-SNAPSHOT") {
        capabilities {
            requireCapability("dev.kord:core-voice:0.8.x-SNAPSHOT")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ru.chimchima.ChimberBotKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
