package ru.chimchima

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.runBlocking
import ru.chimchima.help.HelpServer
import ru.chimchima.properties.DISCORD_TOKEN
import ru.chimchima.properties.LocalProperties
import java.util.concurrent.ConcurrentHashMap

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
    val lastCommands = ConcurrentHashMap<Snowflake, MessageCreateEvent>()

    kord.on<MessageCreateEvent> {
        val author = message.author ?: return@on
        if (author.isBot) return@on

        var event = this
        if (message.content.substringBefore(" ") in listOf("!again", "!rep", "!yadaun")) {
            event = lastCommands[author.id] ?: return@on
        } else {
            lastCommands[author.id] = this
        }

        val command = event.message.content.substringBefore(" ").lowercase()
        when (command) {
            "!plink" -> commands.plink(event)
            "!say", "!tts" -> commands.say(event)
            "!jane" -> commands.say(event, jane = true)
            "!play" -> commands.play(event)
            "!stop", "!стоп" -> commands.stop(event)
            "!skip" -> commands.skip(event)
            "!next" -> commands.next(event)
            "!seek", "!ff" -> commands.seek(event)
            "!back" -> commands.back(event)
            "!queue" -> commands.queue(event)
            "!shuffle" -> commands.shuffle(event)
            "!clear" -> commands.clear(event)
            "!mute" -> commands.mute(event)
            "!current" -> commands.current(event)
            "!status" -> commands.status(event)
            "!repeat" -> commands.repeat(event)
            "!pause" -> commands.pause(event)
            "!unpause", "!resume" -> commands.resume(event)
            "!join" -> commands.join(event)
            "!help" -> commands.help(event)

            "!pirat" -> commands.pirat(event)
            "!cover" -> commands.cover(event)

            "!antihype" -> commands.antihype(event)
            "!nemimohype", "!nemimohypa", "!nemimo" -> commands.nemimohype(event)
            "!hypetrain" -> commands.hypetrain(event)
            "!antihypetrain", "!antipenis" -> commands.antihypetrain(event)

            "!zamay" -> commands.zamay(event)
            "!mrgaslight", "!gaslight" -> commands.mrgaslight(event)
            "!lusthero3", "!lusthero", "!lust" -> commands.lusthero3(event)

            "!slavakpss", "!slava", "!kpss" -> commands.slavakpss(event)
            "!russianfield", "!pole" -> commands.russianfield(event)
            "!bootlegvolume1", "!bootleg" -> commands.bootlegvolume1(event)
            "!angelstrue", "!angel", "!true" -> commands.angelstrue(event)

            "!krovostok", "!krov" -> commands.krovostok(event)
            "!bloodriver", "!blood", "!reka", "!rekakrovi" -> commands.bloodriver(event)
            "!skvoznoe", "!skvoz" -> commands.skvoznoe(event)
            "!dumbbell", "!dumb", "!gantelya" -> commands.dumbbell(event)
            "!studen" -> commands.studen(event)
            "!lombard" -> commands.lombard(event)
            "!cheburashka", "!cheba", "!chb" -> commands.cheburashka(event)
            "!nauka", "!science" -> commands.nauka(event)
            "!krovonew", "!lenin" -> commands.krovonew(event)

            "!snus" -> commands.snus(event)
            "!pauk" -> commands.pauk(event)
            "!sasha" -> commands.sasha(event)
            "!discord" -> commands.discord(event)
            "!sperma" -> commands.sperma(event)
            "!taxi" -> commands.taxi(event)
            "!diss" -> commands.diss(event)
            "!dvar", "!kokiki", "!kotiki", "!котики" -> commands.kokiki(event)
            "!koshechki", "!кошечки" -> commands.koshechki(event)

            "!cocyxa", "!сосуха" -> commands.cocyxa(event)
            "!cocyxa2", "!сосуха2" -> commands.cocyxa2(event)

            "!ruslan" -> commands.ruslan(event)
        }

        if (command.startsWith("!play")) {
            val count = command.substringAfter("!play").toIntOrNull() ?: return@on
            commands.play(event, count)
        }

        if (command.startsWith("!next")) {
            val count = command.substringAfter("!next").toIntOrNull() ?: return@on
            commands.play(event, count, playNext = true)
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
