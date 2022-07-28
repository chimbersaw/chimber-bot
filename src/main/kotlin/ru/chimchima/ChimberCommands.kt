@file:OptIn(KordVoice::class)

package ru.chimchima

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
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
```
Команды:
    !pirat [count] — Добавляет в очередь count (или все доступные) треков Серёги Бандита.
    !shuffled [count] — Добавляет в очередь count (или все доступные) треки Серёги Бандита в случайном порядке.
    !play <track name> — Присоединяется к каналу и воспроизводит композицию с указанным названием (поиск по YouTube).
    !stop — Прекращает воспроизведение очереди и покидает канал.
    !skip [count] — Пропускает следующие count композиций (включая текущую), по умолчанию count=1.
    !queue — Выводит текущую очередь композиций.
    !current — Выводит название текущей композиции.
    !help — Выводит данное сообщение.
```
"""

class Track(
    val title: String,
    private val audioTrack: AudioTrack,
    private val message: Message
) {
    fun playWith(player: AudioPlayer) = player.playTrack(audioTrack)

    suspend fun playingTrack() = message.replyWith("playing track: $title")
    suspend fun queuedTrack() = message.replyWith("queued track: $title")
}

data class Session(
    val connection: VoiceConnection,
    val player: AudioPlayer,
    val queue: LinkedBlockingQueue<Track>
)

class ChimberCommands(private val lavaPlayerManager: LavaPlayerManager) {
    private val piratRepository = PiratRepository()
    private val sessions = ConcurrentHashMap<Snowflake, Session>()

    private suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)?.connection?.shutdown()
    }

    private suspend fun connect(channel: BaseVoiceChannelBehavior): Session {
        val player = lavaPlayerManager.createPlayer()
        val queue = LinkedBlockingQueue<Track>()

        val connection = channel.connect {
            audioProvider {
                sessions[channel.guildId] ?: return@audioProvider null

                var frame = player.provide(1, TimeUnit.SECONDS)

                while (frame == null) {
                    val track = queue.poll(1, TimeUnit.SECONDS) ?: run {
                        disconnect(channel.guildId)
                        return@audioProvider null
                    }
                    track.playWith(player)
                    frame = player.provide(5, TimeUnit.SECONDS)
                    if (frame != null) {
                        track.playingTrack()
                    }
                }

                AudioFrame.fromData(frame.data)
            }
        }

        val session = Session(connection, player, queue)
        sessions[channel.guildId] = session

        return session
    }

    private suspend fun addTrackToQueue(
        event: MessageCreateEvent,
        query: String,
        replyTitle: String? = null,
        quiet: Boolean = false
    ) {
        val guildId = event.guildId ?: return
        val channel = event.member?.getVoiceStateOrNull()?.getChannelOrNull() ?: return

        val audioTrack = lavaPlayerManager.loadTrack(query)
        val title = replyTitle ?: audioTrack.info.title
        val fullTitle = "$title ${audioTrack.formatDuration()}"
        val track = Track(fullTitle, audioTrack, event.message)
        audioTrack.userData = fullTitle

        val connection = sessions[guildId] ?: connect(channel)
        connection.queue.add(track)
        if (!quiet && connection.queue.size > 1) {
            track.queuedTrack()
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
        val count = event.query.toIntOrNull() ?: 1
        if (count < 1) return
        val (_, player, queue) = sessions[event.guildId] ?: return
        val currentTrack = player.playingTrack ?: return

        val skippedSongs = mutableListOf<String>()
        repeat(count - 1) {
            val track = queue.poll() ?: return@repeat
            skippedSongs.add(track.title)
        }
        player.stopTrack()

        currentTrack.getUserData(String::class.java)?.let {
            skippedSongs.add(0, it)
        }

        val skipped = skippedSongs.joinToString(separator = "\n", prefix = "```\n", postfix = "\n```")
        event.message.replyWith("skipped:\n$skipped")
    }

    suspend fun queue(event: MessageCreateEvent) {
        val queue = sessions[event.guildId]?.queue

        val reply = if (queue == null || queue.isEmpty()) {
            "Queue is empty!"
        } else {
            queue.mapIndexed { i, track ->
                "${i + 1}. ${track.title}"
            }.joinToString(separator = "\n", prefix = "Playing next:\n```", postfix = "```")
        }

        event.message.replyWith(reply)
    }

    suspend fun current(event: MessageCreateEvent) {
        val title = sessions[event.guildId]?.player?.playingTrack?.getUserData(String::class.java) ?: return
        event.message.replyWith(title)
    }

    suspend fun help(event: MessageCreateEvent) {
        event.message.replyWith(USAGE)
    }
}
