package ru.chimchima.core

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import java.util.concurrent.ConcurrentHashMap

class MessageHandler {
    private val skipReplyToUserIds = ConcurrentHashMap.newKeySet<Snowflake>()

    suspend fun delete(message: Message?) {
        message?.delete()
    }

    suspend fun replyWith(message: Message, text: String, forcedMessage: Boolean = false): Message? {
        val author = message.author ?: return null
        if (author.data.id in skipReplyToUserIds && !forcedMessage) {
            return null
        }

        return message.reply {
            content = text
        }
    }

    suspend fun replyWith(command: Command, text: String, forcedMessage: Boolean = false): Message? {
        return replyWith(command.message, text, forcedMessage)
    }

    suspend fun mute(command: Command) {
        val author = command.message.author ?: run {
            replyWith(command, "Can't perform \"!mute\" command, author is not defined.", true)
            return
        }

        val userId = author.data.id
        if (userId !in skipReplyToUserIds) {
            skipReplyToUserIds.add(userId)
            replyWith(command, "Bot is muted for you.", true)
        } else {
            skipReplyToUserIds.remove(userId)
            replyWith(command, "Bot is unmuted for you.", true)
        }
    }
}
