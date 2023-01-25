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
import ru.chimchima.player.LavaPlayerManager
import ru.chimchima.repository.AntihypeRepository
import ru.chimchima.repository.PiratRepository
import ru.chimchima.utils.formatDuration
import ru.chimchima.utils.query
import ru.chimchima.utils.replyWith
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

private const val MAX_MESSAGE_LENGTH = 2000
private const val USAGE = """
```
Команды:
    !pirat [count] — Добавляет в очередь count (или все доступные) треки Серёги Бандита.
    !shuffled [count] — Добавляет в очередь count (или все доступные) треки Серёги Бандита в случайном порядке.
    !antihype [count] — Добавляет в очередь count (или все доступные) треки Antihypetrain.
    !antishuffle [count] — Добавляет в очередь count (или все доступные) треки Antihypetrain в случайном порядке.
    !play <track name> — Присоединяется к каналу и воспроизводит композицию с указанным названием (поиск по YouTube).
    !snus [count] - Добавляют в очередь count снюсов.
    !pauk [count] - Добавляют в очередь count пауков.
    !stop — Прекращает воспроизведение очереди и покидает канал.
    !skip [count] — Пропускает следующие count композиций (включая текущую), по умолчанию count=1.
    !queue — Выводит текущую очередь композиций.
    !shuffle — Перемешать очередь композиций.
    !clear — Очистить очередь композиций.
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
    fun clone() = Track(title, audioTrack.makeClone(), message)

    suspend fun playingTrack() = message.replyWith("playing track: $title")
}

data class Session(
    val connection: VoiceConnection,
    val player: AudioPlayer,
    var queue: LinkedBlockingQueue<Track>
)

class ChimberCommands(private val lavaPlayerManager: LavaPlayerManager) {
    private val piratRepository = PiratRepository()
    private val antihypeRepository = AntihypeRepository()
    private val sessions = ConcurrentHashMap<Snowflake, Session>()

    private suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)?.connection?.shutdown()
    }

    private suspend fun connect(channel: BaseVoiceChannelBehavior): Session {
        val player = lavaPlayerManager.createPlayer()

        val connection = channel.connect {
            audioProvider {
                val queue = sessions[channel.guildId]?.queue ?: return@audioProvider null

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

        val session = Session(connection, player, LinkedBlockingQueue<Track>())
        sessions[channel.guildId] = session

        return session
    }

    private suspend fun addTrackToQueue(
        event: MessageCreateEvent,
        query: String,
        replyTitle: String? = null,
        quiet: Boolean = false,
        count: Int = 1
    ) {
        val guildId = event.guildId ?: return
        val channel = event.member?.getVoiceStateOrNull()?.getChannelOrNull() ?: return
        val connection = sessions[guildId] ?: connect(channel)

        val audioTrack = lavaPlayerManager.loadTrack(query) ?: return
        val title = replyTitle ?: audioTrack.info.title
        val fullTitle = "$title ${audioTrack.formatDuration()}"

        val track = Track(fullTitle, audioTrack, event.message)
        audioTrack.userData = fullTitle

        if (!quiet && connection.queue.size + count > 1) {
            val msg = if (count == 1) {
                "queued track: $title"
            } else {
                "queued $count tracks: $title"
            }
            event.message.replyWith(msg)
        }

        repeat(count) {
            connection.queue.add(track.clone())
        }
    }

    suspend fun plink(event: MessageCreateEvent) {
        val message = event.message
        val response = message.channel.createMessage("plonk!")

        delay(5000)
        message.delete()
        response.delete()
    }

    suspend fun play(event: MessageCreateEvent, count: Int = 1) {
        var query = event.query
        if (query.isBlank()) return

        // mne poxui
        if (!query.startsWith("http")) {
            query = "ytsearch: $query"
        }

        addTrackToQueue(event, query, count = count)
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

    suspend fun antihypetrain(event: MessageCreateEvent, shuffled: Boolean = false) {
        val loading = event.message.replyWith("*Добавляю замая...*")

        val count = event.query.toIntOrNull()
        for ((title, url) in antihypeRepository.getSongs(count, shuffled)) {
            addTrackToQueue(event, url, title, quiet = true)
        }

        loading.delete()
        queue(event)
    }

    suspend fun snus(event: MessageCreateEvent) {
        addTrackToQueue(event, "https://www.youtube.com/watch?v=mx-f_wbZTMI", count = event.query.toIntOrNull() ?: 1)
    }

    suspend fun pauk(event: MessageCreateEvent) {
        addTrackToQueue(event, "https://www.youtube.com/watch?v=e2RqDHziN6k", count = event.query.toIntOrNull() ?: 1)
    }

    suspend fun sasha(event: MessageCreateEvent) {
        addTrackToQueue(event, "https://www.youtube.com/watch?v=0vQBaqUPtlc", count = event.query.toIntOrNull() ?: 1)
    }

    suspend fun shuffle(event: MessageCreateEvent) {
        val session = sessions[event.guildId] ?: run {
            event.message.replyWith("Nothing to shuffle.")
            return
        }

        val loading = event.message.replyWith("*Shuffling...*")

        val shuffledQueue = session.queue.shuffled()
        session.queue = LinkedBlockingQueue<Track>(shuffledQueue)

        loading.delete()
        queue(event)
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

        val reply = if (queue.isNullOrEmpty()) {
            "Queue is empty!"
        } else {
            val prefix = "${queue.size + 1} tracks total.\nPlaying next:\n```\n"
            val postfix = "```"
            val threeDots = "...\n"
            val trackList = queue.mapIndexed { i, track ->
                "${i + 1}. ${track.title}\n"
            }
            val lastTrackIndex = trackList.size - 1

            var currentLength = prefix.length + postfix.length
            var currentIndex = 0
            val croppedList = trackList.takeWhile {
                var threshold = MAX_MESSAGE_LENGTH - threeDots.length
                if (currentIndex == lastTrackIndex) {
                    threshold = MAX_MESSAGE_LENGTH
                }

                if (currentLength + it.length <= threshold) {
                    currentLength += it.length
                    currentIndex++
                    true
                } else {
                    false
                }
            }

            var result = prefix + croppedList.joinToString(separator = "")
            if (croppedList.size < trackList.size) {
                result += threeDots
            }
            result += postfix
            result
        }

        event.message.replyWith(reply)
    }

    suspend fun clear(event: MessageCreateEvent) {
        val queue = sessions[event.guildId]?.queue ?: run {
            event.message.replyWith("Queue is already empty.")
            return
        }

        queue.clear()
        event.message.replyWith("Queue cleared.")
    }

    suspend fun current(event: MessageCreateEvent) {
        val title = sessions[event.guildId]?.player?.playingTrack?.getUserData(String::class.java) ?: return
        event.message.replyWith(title)
    }

    suspend fun help(event: MessageCreateEvent) {
        event.message.replyWith(USAGE)
    }
}
