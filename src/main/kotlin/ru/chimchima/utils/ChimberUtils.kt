package ru.chimchima.utils

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

fun formatTime(millis: Long): String {
    val seconds = millis.milliseconds.inWholeSeconds
    return String.format("%02d:%02d", seconds / 60, seconds % 60)
}

fun generateStatusBar(position: Long, duration: Long, barLength: Int = 12): String {
    val progress = (position.toDouble() / duration.toDouble() * barLength).toInt()
    val remaining = barLength - progress

    val progressBar = "▣".repeat(progress)
    val emptyBar = "▢".repeat(remaining)

    return "[$progressBar$emptyBar] ${formatTime(position)} / ${formatTime(duration)}"
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

suspend inline fun HttpResponse.performRequest() = runOnSuccessOrNull<String, Boolean> { true } ?: false

fun String.isHttp() = Regex("https?://.+").matches(this)

fun String.toSignedIntOrNull(allowNegative: Boolean) = if (allowNegative) toIntOrNull() else toUIntOrNull()?.toInt()

fun <T> T.toLoader(): suspend () -> T = { this }

fun <T> T.repeatNTimes(n: Int): List<T> = List(n) { this }

fun <T> List<T>.repeatNTimes(n: Int): List<T> = List(n) { this }.flatten()
