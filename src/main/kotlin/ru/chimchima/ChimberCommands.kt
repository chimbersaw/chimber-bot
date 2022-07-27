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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

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

data class TrackInQueue(
    val player: AudioPlayer,
    val message: Message,
    val title: String
) {
    suspend fun playingTrack() = message.replyWith("playing track: $title")
    suspend fun queuedTrack() = message.replyWith("queued track: $title")
}

data class GuildConnection(
    val connection: VoiceConnection,
    val queue: LinkedBlockingQueue<TrackInQueue>,
    var currentTrack: TrackInQueue
)

class ChimberCommands(private val lavaPlayerManager: LavaPlayerManager) {
    private val piratRepository = PiratRepository()
    private val guildConnections = ConcurrentHashMap<Snowflake, GuildConnection>()

    private suspend fun disconnect(guildId: Snowflake) {
        guildConnections.remove(guildId)?.connection?.shutdown()
    }

    private suspend fun connect(channel: BaseVoiceChannelBehavior, firstTrack: TrackInQueue): GuildConnection {
        val queue = LinkedBlockingQueue<TrackInQueue>()

        val connection = channel.connect {
            audioProvider {
                val guildConnection = guildConnections[channel.guildId] ?: return@audioProvider null

                var frame = guildConnection.currentTrack.player.provide()
                var newTrack = false

                while (frame == null) {
                    guildConnection.currentTrack = queue.poll(1, TimeUnit.SECONDS) ?: run {
                        disconnect(channel.guildId)
                        return@audioProvider null
                    }
                    frame = guildConnection.currentTrack.player.provide()
                    newTrack = true
                }

                if (newTrack) {
                    guildConnection.currentTrack.playingTrack()
                }

                AudioFrame.fromData(frame.data)
            }
        }

        val guildConnection = GuildConnection(connection, queue, firstTrack)
        guildConnections[channel.guildId] = guildConnection

        return guildConnection
    }

    private suspend fun addTrackToQueue(
        event: MessageCreateEvent,
        query: String,
        replyTitle: String? = null,
        quiet: Boolean = false
    ) {
        val guildId = event.guildId ?: return
        val channel = event.member?.getVoiceState()?.getChannelOrNull() ?: return

        val player = lavaPlayerManager.createPlayer()
        val track = lavaPlayerManager.playTrack(query, player)
        val title = replyTitle ?: track.info.title
        val fullTitle = "$title ${track.formatDuration()}"
        val trackInQueue = TrackInQueue(player, event.message, fullTitle)

        val connection = guildConnections[guildId] ?: connect(channel, trackInQueue)
        connection.queue.add(trackInQueue)
        if (!quiet && connection.queue.size > 1) {
            trackInQueue.queuedTrack()
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
        disconnect(guildId)
    }

    suspend fun pirat(event: MessageCreateEvent, shuffled: Boolean = false) {
        val loading = event.message.replyWith("*Добавляю серегу...*")

        val count = event.query.toIntOrNull()
        for ((title, url) in piratRepository.getSongs(count, shuffled)) {
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
        val titles = mutableListOf<String>()
        repeat(count) {
            val track = queue.poll() ?: return@repeat
            titles.add(track.title)
        }
        val skippedTitles = titles.joinToString(separator = "\n", prefix = "```", postfix = "```")
        event.message.replyWith("skipped:\n$skippedTitles")
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
