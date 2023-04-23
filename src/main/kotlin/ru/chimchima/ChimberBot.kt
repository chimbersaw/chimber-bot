package ru.chimchima

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.runBlocking
import ru.chimchima.help.HelpServer
import ru.chimchima.properties.DISCORD_TOKEN
import ru.chimchima.properties.LocalProperties

fun startHelpServer() {
    println("Starting !help server...")
    val port = LocalProperties.port ?: 8080
    HelpServer(port).start()
}

suspend fun main() = runBlocking {
    val token = LocalProperties.discordToken ?: throw RuntimeException("$DISCORD_TOKEN property is not set")
    val kord = Kord(token)
    startHelpServer()

    val commands = ChimberCommands()

    kord.on<MessageCreateEvent> {
        if (message.author?.isBot != false) return@on
        val command = message.content.substringBefore(" ")
        when (command) {
            "!plink" -> commands.plink(this)
            "!say", "!tts" -> commands.say(this)
            "!jane" -> commands.say(this, jane = true)
            "!play" -> commands.play(this)
            "!stop" -> commands.stop(this)
            "!skip", "!next" -> commands.skip(this)
            "!queue" -> commands.queue(this)
            "!shuffle" -> commands.shuffle(this)
            "!clear" -> commands.clear(this)
            "!current" -> commands.current(this)
            "!repeat" -> commands.repeat(this)
            "!pause" -> commands.pause(this)
            "!unpause", "!resume" -> commands.resume(this)
            "!help" -> commands.help(this)

            "!pirat" -> commands.pirat(this)

            "!antihype" -> commands.antihype(this)
            "!nemimohype", "!nemimohypa", "!nemimo" -> commands.nemimohype(this)
            "!hypetrain" -> commands.hypetrain(this)
            "!antihypetrain", "!antipenis" -> commands.antihypetrain(this)

            "!zamay" -> commands.zamay(this)
            "!mrgaslight", "!gaslight" -> commands.mrgaslight(this)
            "!lusthero3", "!lusthero", "!lust" -> commands.lusthero3(this)

            "!slavakpss", "!slava", "!kpss" -> commands.slavakpss(this)
            "!russianfield", "!pole" -> commands.russianfield(this)
            "!bootlegvolume1", "!bootleg" -> commands.bootlegvolume1(this)
            "!angelstrue", "!angel", "!true" -> commands.angelstrue(this)

            "!snus" -> commands.snus(this)
            "!pauk" -> commands.pauk(this)
            "!sasha" -> commands.sasha(this)
            "!ruslan" -> commands.ruslan(this)
        }
        if (command.startsWith("!play")) {
            val count = command.substringAfter("!play").toIntOrNull() ?: return@on
            commands.play(this, count)
        }
    }

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
        presence {
            listening("!help")
        }
        println("Chimber started")
    }
}
