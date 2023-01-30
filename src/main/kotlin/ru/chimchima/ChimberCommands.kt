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
import ru.chimchima.tts.TTSManager
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
    !play[count] <track name / url> — Присоединяется к каналу и воспроизводит 1 (или count) композиций с указанным названием (поиск по YouTube) / по указанной ссылке.
    !stop — Прекращает воспроизведение очереди и покидает канал.
    !skip [count] — Пропускает следующие count композиций (включая текущую), по умолчанию count=1.
    !queue — Выводит текущую очередь композиций.
    !shuffle — Перемешать очередь композиций.
    !clear — Очистить очередь композиций.
    !current — Выводит название текущей композиции.
    !repeat [on/off] — Устанавливает режим повторения трека на переданный (выводит текущий при отсутствии аргументов).
    !pause - Ставит текущий трек на паузу.
    !resume - Снимает текущий трек с паузы.
    !help — Выводит данное сообщение.

    !say <text> - Произносит текст рандомным голосом вне очереди.
    !jane <text> - Произносит текст голосом злой Жени вне очереди.
    !pirat [count] — Добавляет в очередь count (или все доступные) треки Серёги Бандита.
    !shuffled [count] — Добавляет в очередь count (или все доступные) треки Серёги Бандита в случайном порядке.
    !antihype [count] — Добавляет в очередь count (или все доступные) треки Antihypetrain.
    !antishuffle [count] — Добавляет в очередь count (или все доступные) треки Antihypetrain в случайном порядке.
    !snus [count] - Добавляют в очередь count снюсов.
    !pauk [count] - Добавляют в очередь count пауков.
    !sasha [count] - Добавляют в очередь count саш.
```
"""

class Track(
    private val message: Message,
    private val audioTrack: AudioTrack,
    val title: String
) {
    fun playWith(player: AudioPlayer) = player.playTrack(audioTrack)
    fun clone() = Track(message, audioTrack.makeClone(), title)

    suspend fun playingTrack() = message.replyWith("playing track: $title")

    companion object {
        suspend fun builder(message: Message, query: String, title: String? = null): suspend () -> Track? =
            {
                LavaPlayerManager.loadTrack(query)?.let {
                    val fullTitle = "${title ?: it.info.title} ${it.formatDuration()}"
                    Track(message, it, fullTitle)
                }
            }
    }
}

data class Session(
    val player: AudioPlayer,
    var queue: LinkedBlockingQueue<Track>,
    var ttsQueue: LinkedBlockingQueue<Track>,
    var current: Track? = null,
)

enum class Repeat {
    ON,
    OFF
}

enum class Pause {
    ON,
    OFF
}

@OptIn(KordVoice::class)
class ChimberCommands {
    private val ttsManager = TTSManager()

    private val connections = ConcurrentHashMap<Snowflake, VoiceConnection>()
    private val sessions = ConcurrentHashMap<Snowflake, Session>()
    private val repeats = ConcurrentHashMap<Snowflake, Repeat>()
    private val pauses = ConcurrentHashMap<Snowflake, Pause>()

    private suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)
        connections.remove(guildId)?.shutdown()
        pauses.remove(guildId)
    }

    private suspend fun connect(channel: BaseVoiceChannelBehavior): Session {
        val player = LavaPlayerManager.createPlayer()
        val queue = LinkedBlockingQueue<Track>()
        val ttsPlayer = LavaPlayerManager.createPlayer()
        val ttsQueue = LinkedBlockingQueue<Track>()

        val session = Session(player, queue, ttsQueue)

        val guildId = channel.guildId

        if (!repeats.containsKey(guildId)) {
            repeats[guildId] = Repeat.OFF
        }

        if (!pauses.containsKey(guildId)) {
            pauses[guildId] = Pause.OFF
        }

        val connection = channel.connect {
            audioProvider {
                if (pauses[guildId] == Pause.ON) {
                    return@audioProvider AudioFrame.SILENCE
                }

                ttsPlayer.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }

                session.ttsQueue.poll()?.let {
                    it.playWith(ttsPlayer)
                    return@audioProvider AudioFrame.SILENCE
                }

                val frame = player.provide(1, TimeUnit.SECONDS)

                if (frame == null) {
                    if (repeats[guildId] == Repeat.OFF || session.current == null) {
                        session.current = queue.poll(100, TimeUnit.MILLISECONDS)
                        session.current?.playingTrack()
                    }

                    val track = session.current?.clone()

                    if (track == null) {
                        disconnect(guildId)
                        return@audioProvider null
                    }

                    track.playWith(player)
                    return@audioProvider AudioFrame.SILENCE
                }

                AudioFrame.fromData(frame.data)
            }
        }

        sessions[channel.guildId] = session
        connections[channel.guildId] = connection

        return session
    }

    private suspend fun addTracksToQueue(
        event: MessageCreateEvent,
        builders: List<suspend () -> Track?>
    ): Int {
        val guildId = event.guildId ?: return 0
        val channel = event.member?.getVoiceStateOrNull()?.getChannelOrNull() ?: return 0
        val session = sessions[guildId] ?: connect(channel)

        var queueSize = session.queue.size
        if (session.current == null) {
            queueSize--
        }

        for (builder in builders) {
            builder.invoke()?.let {
                session.queue.add(it)
            } ?: event.replyWith("Loading tracks failed...")
        }

        return queueSize + builders.size
    }

    private suspend fun addTrackToQueue(event: MessageCreateEvent, query: String, count: Int = 1) {
        val track = Track.builder(event.message, query).invoke() ?: run {
            event.replyWith("No such track was found :(")
            return
        }

        val builder: suspend () -> Track? = { track.clone() }
        val queueSize = addTracksToQueue(event, List(count) { builder })

        if (queueSize > 0) {
            val msg = if (count == 1) {
                "queued track: ${track.title}"
            } else {
                "queued $count tracks: ${track.title}"
            }
            event.replyWith(msg)
        }
    }

    suspend fun plink(event: MessageCreateEvent) {
        val message = event.message
        val response = message.channel.createMessage("plonk!")

        delay(5000)
        message.delete()
        response.delete()
    }

    suspend fun say(event: MessageCreateEvent, jane: Boolean = false) {
        val query = event.query
        if (query.isBlank()) return
        if (query.length > 500) {
            event.replyWith("!say query must be no longer than 500 symbols")
            return
        }

        val file = ttsManager.textToSpeech(query, jane) ?: run {
            event.replyWith("Could not load tts :(")
            return
        }

        val guildId = event.guildId ?: return
        val channel = event.member?.getVoiceStateOrNull()?.getChannelOrNull() ?: return
        val session = sessions[guildId] ?: connect(channel)

        val track = Track.builder(event.message, file.absolutePath, query).invoke() ?: run {
            event.replyWith("Couldn't load tts :(")
            return
        }

        session.ttsQueue.add(track)
    }

    suspend fun play(event: MessageCreateEvent, count: Int = 1) {
        var query = event.query
        if (query.isBlank()) return

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
        val loading = event.replyWith("*Добавляю серегу...*")

        val count = event.query.toIntOrNull()
        val builders = PiratRepository.getBuilders(event.message, count, shuffled)
        addTracksToQueue(event, builders)

        loading.delete()
        if (connections.containsKey(event.guildId)) {
            queue(event)
        }
    }

    suspend fun antihypetrain(event: MessageCreateEvent, shuffled: Boolean = false) {
        val loading = event.replyWith("*Добавляю замая...*")

        val count = event.query.toIntOrNull()
        val builders = AntihypeRepository.getBuilders(event.message, count, shuffled)
        addTracksToQueue(event, builders)

        loading.delete()
        if (connections.containsKey(event.guildId)) {
            queue(event)
        }
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
            event.replyWith("Nothing to shuffle.")
            return
        }

        val loading = event.replyWith("*Shuffling...*")

        val shuffledQueue = session.queue.shuffled()
        session.queue = LinkedBlockingQueue<Track>(shuffledQueue)

        loading.delete()
        queue(event)
    }

    suspend fun skip(event: MessageCreateEvent) {
        val count = event.query.toIntOrNull() ?: 1
        if (count < 1) return

        val (player, queue, _, current) = sessions[event.guildId] ?: return
        if (current == null) return

        val skippedTracks = mutableListOf(current.title)
        repeat(count - 1) {
            val track = queue.poll() ?: return@repeat
            skippedTracks.add(track.title)
        }
        player.stopTrack()
        sessions[event.guildId]?.current = null

        val skipped = skippedTracks.joinToString(separator = "\n", prefix = "```\n", postfix = "\n```")
        event.replyWith("skipped:\n$skipped")
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

        event.replyWith(reply)
    }

    suspend fun clear(event: MessageCreateEvent) {
        val queue = sessions[event.guildId]?.queue ?: run {
            event.replyWith("Queue is already empty.")
            return
        }

        queue.clear()
        event.replyWith("Queue cleared.")
    }

    suspend fun current(event: MessageCreateEvent) {
        val title = sessions[event.guildId]?.current?.title ?: return
        event.replyWith(title)
    }

    suspend fun repeat(event: MessageCreateEvent) {
        val guildId = event.guildId ?: return

        var start = "Repeat is now"
        when (event.query.lowercase()) {
            "on" -> repeats[guildId] = Repeat.ON
            "off" -> repeats[guildId] = Repeat.OFF
            else -> start = "Repeat is"
        }

        val mode = (repeats[guildId] ?: Repeat.OFF).toString().lowercase()
        event.replyWith("$start $mode.")
    }

    suspend fun pause(event: MessageCreateEvent) {
        val guildId = event.guildId ?: return
        pauses[guildId] = Pause.ON
        event.replyWith("Player is paused.")
    }

    suspend fun resume(event: MessageCreateEvent) {
        val guildId = event.guildId ?: return
        pauses[guildId] = Pause.OFF
        event.replyWith("Player is resumed.")
    }

    suspend fun help(event: MessageCreateEvent) {
        event.replyWith(USAGE)
    }
}
