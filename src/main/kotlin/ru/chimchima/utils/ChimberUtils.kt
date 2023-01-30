package ru.chimchima.utils

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent

suspend fun Message.replyWith(text: String) = reply { content = text }
suspend fun MessageCreateEvent.replyWith(text: String) = message.replyWith(text)

val MessageCreateEvent.query: String
    get() {
        val query = message.content
        return if (query.startsWith("!")) {
            query.substringAfter(delimiter = " ", missingDelimiterValue = "").trim()
        } else {
            ""
        }
    }

fun AudioTrack.formatDuration(): String {
    val durationInSeconds = duration / 1000
    val minutes = String.format("%02d", durationInSeconds / 60)
    val seconds = String.format("%02d", durationInSeconds % 60)
    return "$minutes:$seconds"
}
