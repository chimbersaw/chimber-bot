package ru.chimchima

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.delay
import ru.chimchima.properties.DISCORD_TOKEN
import ru.chimchima.properties.LocalProperties
import ru.chimchima.server.PingServer

fun startPingServer() {
    val port = LocalProperties.port ?: 8080
    PingServer(port).start()
}

suspend fun main() {
    val token = LocalProperties.discordToken ?: throw RuntimeException("$DISCORD_TOKEN is not set")
    val kord = Kord(token)
    startPingServer()

    kord.on<MessageCreateEvent> {
        if (message.author?.isBot != false) return@on
        if (message.content != "!plink") return@on

        val response = message.channel.createMessage("plonk!")

        delay(5000)
        message.delete()
        response.delete()
    }

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }
}
