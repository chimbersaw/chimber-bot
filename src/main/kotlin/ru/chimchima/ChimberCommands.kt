@file:OptIn(KordVoice::class)

package ru.chimchima

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import kotlinx.coroutines.delay
import ru.chimchima.pirat.songs
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

private const val USAGE = """
```Команды:
    !pirat [count] — Добавляет в очередь count (или все доступные) треков Серёги Бандита.
    !shuffled [count] — Добавляет в очередь count (или все доступные) треки Серёги Бандита в случайном порядке.
    !play <track name> — Присоединяется к каналу и воспроизводит композицию с указанным названием (поиск по YouTube).
    !stop — Прекращает воспроизведение очереди и покидает канал.
    !skip [count] — Пропускает следующие count композиций (включая текущую), по умолчанию count=1.
    !queue — Выводит текущую очередь композиций.
    !help — Выводит данное сообщение.```
"""

data class QueuedTrack(
    val query: String,
    val player: AudioPlayer,
    val message: Message,
    val title: String,
    val quiet: Boolean
) {
    private suspend fun reply(text: String) {
        message.reply {
            content = text
        }
    }

    suspend fun playingTrack() = reply("playing track: $title")
    suspend fun queuedTrack() {
        if (!quiet) {
            reply("queued track: $title")
        }
    }
}

data class GuildConnection(
    val connection: VoiceConnection,
    val queue: ConcurrentLinkedQueue<QueuedTrack>
)

class ChimberCommands(private val lavaPlayerManager: LavaPlayerManager) {
    private val guildConnections = ConcurrentHashMap<ULong, GuildConnection>()

    private suspend fun disconnect(guildId: ULong, force: Boolean = false) {
        guildConnections[guildId]?.let { (connection, queue) ->
            if (force || queue.isEmpty()) {
                connection.shutdown()
            }
        }
        guildConnections.remove(guildId)
    }

    private suspend fun connect(channel: BaseVoiceChannelBehavior, playFirst: QueuedTrack) {
        val queue = ConcurrentLinkedQueue<QueuedTrack>()
        queue.add(playFirst)
        playFirst.playingTrack()

        val connection = channel.connect {
            audioProvider {
                var queuedTrack = queue.firstOrNull()
                if (queuedTrack == null) {
                    disconnect(channel.guildId.value)
                    return@audioProvider null
                } else if (queuedTrack.player.playingTrack == null) {
                    lavaPlayerManager.playTrack(queuedTrack.query, queuedTrack.player)
                }

                var frame = queuedTrack.player.provide()
                var newTrack = false

                while (frame == null) {
                    queue.poll()
                    queuedTrack = queue.firstOrNull()
                    if (queuedTrack == null) {
                        disconnect(channel.guildId.value)
                        return@audioProvider null
                    } else if (queuedTrack.player.playingTrack == null) {
                        lavaPlayerManager.playTrack(queuedTrack.query, queuedTrack.player)
                    }
                    frame = queuedTrack.player.provide()
                    newTrack = true
                }

                if (newTrack) {
                    queuedTrack?.playingTrack()
                }

                AudioFrame.fromData(frame.data)
            }
        }

        guildConnections[channel.guildId.value] = GuildConnection(connection, queue)
    }

    private suspend fun addTrackToQueue(
        event: MessageCreateEvent,
        query: String,
        replyTitle: String? = null,
        quiet: Boolean = false
    ) {
        val channel = event.member?.getVoiceState()?.getChannelOrNull() ?: return
        val guildId = event.guildId?.value ?: return
        val message = event.message

        val player = lavaPlayerManager.createPlayer()
        val track = lavaPlayerManager.playTrack(query, player)
        player.stopTrack()
        val title = replyTitle ?: track.info.title
        val duration = track.duration / 1000
        val minutes = String.format("%02d", duration / 60)
        val seconds = String.format("%02d", duration % 60)
        val fullTitle = "$title ($minutes:$seconds)"

        val queuedTrack = QueuedTrack(query, player, message, fullTitle, quiet)
        val guildConnection = guildConnections[guildId]

        if (guildConnection == null || guildConnection.queue.isEmpty()) {
            connect(channel, queuedTrack)
        } else {
            guildConnections[guildId]?.queue?.add(queuedTrack)
            queuedTrack.queuedTrack()
        }
    }

    private suspend fun Int.playPiratSong(event: MessageCreateEvent) {
        val (url, title) = songs[this]
        val fileName = "/pirat/${this + 1}.mp3"
        val path = ChimberCommands::class.java.getResource(fileName)?.path ?: "ytsearch: $url"
        addTrackToQueue(event, path, title, quiet = true)
    }

    suspend fun plink(event: MessageCreateEvent) {
        val message = event.message
        val response = message.channel.createMessage("plonk!")

        delay(5000)
        message.delete()
        response.delete()
    }

    suspend fun play(event: MessageCreateEvent) {
        val query = event.message.content.removePrefix("!play").trim()
        addTrackToQueue(event, "ytsearch: $query")
    }

    suspend fun stop(event: MessageCreateEvent) {
        val guildId = event.guildId?.value ?: return
        disconnect(guildId, force = true)
    }

    suspend fun pirat(event: MessageCreateEvent) {
        val loading = event.message.reply {
            content = "*Добавляю серегу...*"
        }
        val count = event.message.content.removePrefix("!pirat").trim().toIntOrNull() ?: 13
        for (i in (0..12).take(count)) {
            i.playPiratSong(event)
        }
        loading.delete()
        queue(event)
    }

    suspend fun shuffled(event: MessageCreateEvent) {
        val loading = event.message.reply {
            content = "*Добавляю серегу...*"
        }
        val count = event.message.content.removePrefix("!shuffled").trim().toIntOrNull() ?: 13
        for (i in (0..12).shuffled().take(count)) {
            i.playPiratSong(event)
        }
        loading.delete()
        queue(event)
    }

    suspend fun skip(event: MessageCreateEvent) {
        val count = event.message.content.removePrefix("!skip").trim().toIntOrNull() ?: 1
        val id = event.guildId?.value
        repeat(count) {
            guildConnections[id]?.queue?.poll()?.let {
                event.message.reply {
                    content = "skipped ${it.title}"
                }
            }
        }
    }

    suspend fun queue(event: MessageCreateEvent) {
        val queue = guildConnections[event.guildId?.value]?.queue
        val reply = if (queue == null || queue.isEmpty()) {
            "Queue is empty!"
        } else {
            queue.mapIndexed { i, track ->
                "${i + 1}. ${track.title}"
            }.joinToString(separator = "\n", prefix = "```", postfix = "```")
        }

        event.message.reply {
            content = reply
        }
    }

    suspend fun help(event: MessageCreateEvent) {
        event.message.reply {
            content = USAGE
        }
    }
}
