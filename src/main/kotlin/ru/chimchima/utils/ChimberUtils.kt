package ru.chimchima.utils

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.util.concurrent.ConcurrentHashMap

suspend fun Message.replyWith(text: String) = reply { content = text }
suspend fun MessageCreateEvent.replyWith(text: String) = message.replyWith(text)

class MessageHandler {
    private val skipReplyToUserIds = ConcurrentHashMap.newKeySet<Snowflake>()

    suspend fun messageReplyWith(message: Message, text: String) {
        val author = message.author?: return
        if (author.data.id in skipReplyToUserIds) {
            return
        }

        message.replyWith(text)
    }

    suspend fun eventReplyWith(event: MessageCreateEvent, text: String) {
        messageReplyWith(event.message, text)
    }

    suspend fun mute(event: MessageCreateEvent): Boolean {
        val author = event.message.author
        if (author == null) {
            event.replyWith("Can't perform \"!mute\" command.")
            return
        }

        val userId = author!!.data.id
        if (userId !in skipReplyToUserIds) {
            skipReplyToUserIds.add(userId)
            event.replyWith("Bot is muted.")
        } else {
            skipReplyToUserIds.remove(userId)
            event.replyWith("Bot is unmuted.")
        }

        return false
    }
}

val MessageCreateEvent.args: String
    get() {
        val query = message.content
        return if (query.startsWith("!")) {
            query.substringAfter(delimiter = " ", missingDelimiterValue = "").trim()
        } else {
            ""
        }
    }

val MessageCreateEvent.query: String
    get() = if (args.isHttp()) {
        args.substringBefore(" ")
    } else {
        args
    }

fun AudioTrack.formatDuration(): String {
    val durationInSeconds = duration / 1000
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
