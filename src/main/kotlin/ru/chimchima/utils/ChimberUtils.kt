package ru.chimchima.utils

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ru.chimchima.core.Command
import kotlin.time.Duration.Companion.milliseconds

val Command.content: String
    get() {
        val query = message.content
        return if (query.startsWith("!")) {
            query.substringAfter(delimiter = " ", missingDelimiterValue = "").trim()
        } else {
            ""
        }
    }

val Command.query: String
    get() = if (content.isHttp()) {
        content.substringBefore(" ")
    } else {
        content
    }

fun AudioTrack.formatDuration(): String {
    val durationInSeconds = duration.milliseconds.inWholeSeconds
    val minutes = String.format("%02d", durationInSeconds / 60)
    val seconds = String.format("%02d", durationInSeconds % 60)
    return "$minutes:$seconds"
}

suspend inline fun <reified T, R> HttpResponse.runOnSuccessOrNull(block: (T) -> R?): R? {
    return if (status.isSuccess()) {
        block(body())
    } else {
        println("*******************************")
        println("Request failed:")
        println("> ${request.method.value} ${request.url}")
        println("> ${request.content}")
        println("< $status")
        println("< ${bodyAsText()}")
        println("*******************************\n")
        null
    }
}

fun String.isHttp() = Regex("https?://.+").matches(this)

fun String.toSignedIntOrNull(allowNegative: Boolean) = if (allowNegative) toIntOrNull() else toUIntOrNull()?.toInt()

fun <T> T.toLoader(): suspend () -> T = { this }

fun <T> T.repeatNTimes(n: Int): List<T> = List(n) { this }

fun <T> List<T>.repeatNTimes(n: Int): List<T> = List(n) { this }.flatten()
