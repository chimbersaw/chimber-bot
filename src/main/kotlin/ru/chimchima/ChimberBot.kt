package ru.chimchima

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.runBlocking
import ru.chimchima.core.ChimberCommands
import ru.chimchima.core.Command
import ru.chimchima.help.HelpServer
import ru.chimchima.utils.LocalProperties
import java.util.concurrent.ConcurrentHashMap

fun startHelpServer() {
    println("Starting !help server...")
    val port = LocalProperties.port ?: 8080
    HelpServer(port).start()
}

fun main() = runBlocking {
    val token = LocalProperties.discordToken ?: throw RuntimeException("DISCORD_TOKEN property is not set")
    val kord = Kord(token)
    startHelpServer()

    val chimber = ChimberCommands()
    val prevPlay = ConcurrentHashMap<Snowflake, Command>()

    kord.on<VoiceStateUpdateEvent> {
        chimber.onVoiceStateUpdate(event = this)
    }

    kord.on<MessageCreateEvent> {
        val author = message.author ?: return@on
        if (author.isBot) return@on

        val command = Command.create(this, prevPlay[author.id]) ?: return@on
        if (command.isPlay) {
            prevPlay[author.id] = command
        }

        when (command.name) {
            "!plink" -> chimber.plink(command)

            "!play" -> chimber.play(command)
            "!stop", "!стоп" -> chimber.stop(command)
            "!skip" -> chimber.skip(command)
            "!next" -> chimber.next(command)
            "!queue" -> chimber.queue(command, forcedMessage = true)
            "!current", "!hp" -> chimber.current(command)
            "!status", "!st" -> chimber.status(command)

            "!mute" -> chimber.mute(command)
            "!repeat" -> chimber.repeat(command)
            "!pause" -> chimber.pause(command)
            "!resume", "!unpause" -> chimber.resume(command)
            "!stay" -> chimber.stay(command)
            "!join" -> chimber.join(command)

            "!seek", "!ff" -> chimber.seek(command)
            "!back" -> chimber.back(command)
            "!shuffle" -> chimber.shuffle(command)
            "!reverse" -> chimber.reverse(command)
            "!clear" -> chimber.clear(command)
            "!help" -> chimber.help(command)

            "!say", "!tts" -> chimber.say(command)
            "!jane" -> chimber.say(command, jane = true)

            "!ruslan" -> chimber.ruslan(command)
            "!vlad" -> chimber.vlad(command)
            "!fallout" -> chimber.fallout(command)
            "!zov", "!svo" -> chimber.zov(command)

            "!pirat" -> chimber.pirat(command)
            "!cover" -> chimber.cover(command)

            "!antihype" -> chimber.antihype(command)
            "!nemimohype", "!nemimohypa", "!nemimo" -> chimber.nemimohype(command)
            "!hypetrain" -> chimber.hypetrain(command)
            "!antihypetrain", "!antipenis" -> chimber.antihypetrain(command)

            "!zamay" -> chimber.zamay(command)
            "!mrgaslight", "!gaslight" -> chimber.mrgaslight(command)
            "!lusthero3", "!lusthero", "!lust" -> chimber.lusthero3(command)

            "!slavakpss", "!slava", "!kpss" -> chimber.slavakpss(command)
            "!russianfield", "!pole" -> chimber.russianfield(command)
            "!bootlegvolume1", "!bootleg" -> chimber.bootlegvolume1(command)
            "!angelstrue", "!angel", "!true" -> chimber.angelstrue(command)

            "!krovostok", "!krov" -> chimber.krovostok(command)
            "!bloodriver", "!blood", "!reka", "!rekakrovi" -> chimber.bloodriver(command)
            "!skvoznoe", "!skvoz" -> chimber.skvoznoe(command)
            "!dumbbell", "!dumb", "!gantelya" -> chimber.dumbbell(command)
            "!studen" -> chimber.studen(command)
            "!lombard" -> chimber.lombard(command)
            "!cheburashka", "!cheba", "!chb" -> chimber.cheburashka(command)
            "!nauka", "!science" -> chimber.nauka(command)
            "!krovonew", "!lenin" -> chimber.krovonew(command)

            "!snus" -> chimber.snus(command)
            "!pauk" -> chimber.pauk(command)
            "!sasha" -> chimber.sasha(command)
            "!discord" -> chimber.discord(command)
            "!sperma" -> chimber.sperma(command)
            "!taxi" -> chimber.taxi(command)
            "!diss" -> chimber.diss(command)
            "!kotiki", "!kokiki", "!котики", "!dvar" -> chimber.kokiki(command)
            "!кошечки", "!koshechki" -> chimber.koshechki(command)
            "!satana", "!сатана" -> chimber.satana(command)

            "!cocyxa", "!сосуха" -> chimber.cocyxa(command)
            "!cocyxa2", "!сосуха2" -> chimber.cocyxa2(command)
            "!raketa", "!zxkoncepba" -> chimber.raketa(command)
            "!val", "!zota", "!lowgrades" -> chimber.lowgrades(command)
            "!valera", "val2" -> chimber.valera(command)
            "!val0" -> chimber.val0(command)
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
