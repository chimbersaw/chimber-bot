@file:OptIn(KordVoice::class)

package ru.chimchima

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import kotlinx.coroutines.delay

class ChimberCommands(private val lavaPlayerManager: LavaPlayerManager) {
    private val connections: MutableMap<Snowflake, VoiceConnection> = mutableMapOf()

    private suspend fun playTrack(event: MessageCreateEvent, query: String, replyWith: String? = null) {
        val channel = event.member?.getVoiceState()?.getChannelOrNull() ?: return
        val guildId = event.guildId ?: return
        val message = event.message

        connections.remove(guildId)?.shutdown()

        val player = lavaPlayerManager.createPlayer()
        val track = lavaPlayerManager.playTrack(query, player)
        val title = track.info.title

        val connection = channel.connect {
            audioProvider {
                AudioFrame.fromData(player.provide()?.data)
            }
        }

        connections[guildId] = connection

        message.reply {
            content = replyWith ?: "playing track: $title"
        }
    }

    suspend fun plink(event: MessageCreateEvent) {
        val message = event.message
        val response = message.channel.createMessage("plonk!")

        delay(5000)
        message.delete()
        response.delete()
    }

    suspend fun play(event: MessageCreateEvent) {
        val query = event.message.content.removePrefix("!play ")
        playTrack(event, "ytsearch: $query")
    }

    suspend fun stop(event: MessageCreateEvent) {
        val guildId = event.guildId ?: return

        connections.remove(guildId)?.shutdown()
    }

    suspend fun pirat(event: MessageCreateEvent) {
        val path = this::class.java.getResource("/pirat/1.mp3")?.path ?: return

        playTrack(event, path, "playing serega)")
    }
}
