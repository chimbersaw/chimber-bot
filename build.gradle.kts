import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.jar.JarFile

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

group = "ru.chimchima"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    implementation("dev.kord:kord-core:0.15.0")
    implementation("dev.kord:kord-core-voice:0.15.0")

    implementation("dev.arbjerg:lavaplayer:2.2.3")
    implementation("com.github.lavalink-devs.youtube-source:v2:-SNAPSHOT") {
        isChanging = true
    }

    implementation("io.ktor:ktor-client-core:3.1.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ru.chimchima.ChimberBotKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
val trackYoutubeSourceVersion by tasks.registering {
    doLast {
        val targetGroup = "com.github.lavalink-devs.youtube-source"
        val targetModule = "v2"
        val versionFile = layout.buildDirectory.file("youtube-source-version.txt").get().asFile
        val configuration = configurations.runtimeClasspath.get()
        configuration.resolve() // ensure resolved
        val resolved = configuration.resolvedConfiguration.resolvedArtifacts.find {
            it.moduleVersion.id.group == targetGroup && it.name == targetModule
        }
        val currentVersion = resolved?.file?.let { jar ->
            JarFile(jar).use { jf ->
                val entry = jf.getJarEntry("yts-version.txt")
                if (entry != null) {
                    jf.getInputStream(entry).bufferedReader().use { it.readText().trim().substringBefore('-') }
                } else null
            }
        }
        if (currentVersion != null) {
            val previousVersion = if (versionFile.exists()) versionFile.readText().trim() else null
            logger.lifecycle("Current youtube source commit: $currentVersion")
            if (previousVersion != null && previousVersion != currentVersion) {
                logger.lifecycle("Youtube source dependency updated: $previousVersion -> $currentVersion")
            }
            versionFile.writeText(currentVersion)
        }
    }
}

tasks.named("build") {
    finalizedBy(trackYoutubeSourceVersion)
}
