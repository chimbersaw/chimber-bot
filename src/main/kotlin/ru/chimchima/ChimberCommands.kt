@file:OptIn(KordVoice::class)

package ru.chimchima

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import kotlinx.coroutines.delay
import ru.chimchima.pirat.PiratRepository
import ru.chimchima.utils.formatDuration
import ru.chimchima.utils.query
import ru.chimchima.utils.replyWith
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
    suspend fun playingTrack() = message.replyWith("playing track: $title")
    suspend fun queuedTrack() {
        if (!quiet) {
            message.replyWith("queued track: $title")
        }
    }
}

data class GuildConnection(
    val connection: VoiceConnection,
    val queue: ConcurrentLinkedQueue<QueuedTrack>
)

class ChimberCommands(private val lavaPlayerManager: LavaPlayerManager) {
    private val piratRepository = PiratRepository()
    private val guildConnections = ConcurrentHashMap<Snowflake, GuildConnection>()

    private suspend fun disconnect(guildId: Snowflake, force: Boolean = false) {
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
                    disconnect(channel.guildId)
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
                        disconnect(channel.guildId)
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

        guildConnections[channel.guildId] = GuildConnection(connection, queue)
    }

    private suspend fun addTrackToQueue(
        event: MessageCreateEvent,
        query: String,
        replyTitle: String? = null,
        quiet: Boolean = false
    ) {
        val channel = event.member?.getVoiceState()?.getChannelOrNull() ?: return
        val guildId = event.guildId ?: return

        val player = lavaPlayerManager.createPlayer()
        val track = lavaPlayerManager.playTrack(query, player)
        val title = replyTitle ?: track.info.title
        val fullTitle = "$title ${track.formatDuration()}"

        val queuedTrack = QueuedTrack(query, player, event.message, fullTitle, quiet)
        val guildConnection = guildConnections[guildId]

        if (guildConnection == null || guildConnection.queue.isEmpty()) {
            connect(channel, queuedTrack)
        } else {
            guildConnections[guildId]?.queue?.add(queuedTrack)
            queuedTrack.queuedTrack()
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
        val query = event.query
        if (query.isNotEmpty()) {
            addTrackToQueue(event, "ytsearch: $query")
        }
    }

    suspend fun stop(event: MessageCreateEvent) {
        val guildId = event.guildId ?: return
        disconnect(guildId, force = true)
    }

    suspend fun pirat(event: MessageCreateEvent, shuffled: Boolean = false) {
        val loading = event.message.replyWith("*Добавляю серегу...*")

        val count = event.query.toIntOrNull() ?: 13
        var ids = (0..12).take(count)
        if (shuffled) {
            ids = ids.shuffled()
        }

        for (i in ids) {
            val (title, url) = piratRepository.getPiratSong(i)
            addTrackToQueue(event, url, title, quiet = true)
        }

        loading.delete()
        queue(event)
    }

    suspend fun shuffled(event: MessageCreateEvent) {
        pirat(event, shuffled = true)
    }

    suspend fun skip(event: MessageCreateEvent) {
        val queue = guildConnections[event.guildId]?.queue ?: return
        val count = event.query.toIntOrNull() ?: 1
        repeat(count) {
            val track = queue.poll() ?: return
            event.message.replyWith("skipped ${track.title}")
        }
    }

    suspend fun queue(event: MessageCreateEvent) {
        val queue = guildConnections[event.guildId]?.queue

        val reply = if (queue == null || queue.isEmpty()) {
            "Queue is empty!"
        } else {
            queue.mapIndexed { i, track ->
                "${i + 1}. ${track.title}"
            }.joinToString(separator = "\n", prefix = "```", postfix = "```")
        }

        event.message.replyWith(reply)
    }

    suspend fun help(event: MessageCreateEvent) {
        event.message.replyWith(USAGE)
    }
}
