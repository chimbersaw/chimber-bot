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
import ru.chimchima.repository.*
import ru.chimchima.tts.TTSManager
import ru.chimchima.utils.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

private const val MAX_MESSAGE_LENGTH = 2000
const val USAGE = """Команды:
    !play[count] <track name / track url / playlist url> — Присоединяется к каналу и воспроизводит 1 (или count) треков/плейлистов с указанным названием (поиск по YouTube) / по указанной ссылке.
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

    !snus [count] - Добавляют в очередь count снюсов.
    !pauk [count] - Добавляют в очередь count пауков.
    !sasha [count] - Добавляют в очередь count саш.

    !<playlist> [-s/--shuffle/--shuffled] [-a/--all/--full] [count]
    Добавляет count (или все) избранных треков из плейлиста (--all для всех треков, --shuffled для случайного порядка треков).

    Плейлисты:
    !pirat - Избранные треки сереги бандита.

    !antihype - Три микстейпа ниже вместе.
    !nemimohype, !nemimohypa, !nemimo - #НЕМИМОХАЙПА (Mixtape)
    !hypetrain - HYPE TRAIN (Mixtape)
    !antihypetrain, !antipenis - ANTIHYPETRAIN

    !zamay - Два альбома ниже вместе.
    !mrgaslight, !gaslight - Mr. Gaslight
    !lusthero3, !lusthero, !lust - LUST HERO 3

    !slavakpss, !slava, !kpss - Три релиза ниже вместе.
    !russianfield, !pole - Русское поле (Бутер Бродский)
    !bootlegvolume1, !bootleg - Bootleg Vol.1
    !angelstrue, !angel, !true - Ангельское True (Mixtape)

"""

typealias PlaylistLoader = suspend () -> List<Track>
typealias TrackLoader = suspend () -> Track?

class Track(
    private val message: Message,
    private val audioTrack: AudioTrack,
    val title: String
) {
    fun playWith(player: AudioPlayer) = player.playTrack(audioTrack)
    fun clone() = Track(message, audioTrack.makeClone(), title)

    suspend fun playingTrack() = message.replyWith("playing track: $title")

    companion object {
        private fun AudioTrack.toTrack(message: Message, title: String? = null): Track {
            val fullTitle = "${title ?: info.title} ${formatDuration()}"
            return Track(message, this, fullTitle)
        }

        suspend fun trackLoader(message: Message, query: String, title: String? = null): TrackLoader = {
            LavaPlayerManager.loadTrack(query)?.toTrack(message, title)
        }

        suspend fun playlistLoader(message: Message, query: String, title: String? = null): PlaylistLoader = {
            LavaPlayerManager.loadPlaylist(query).map {
                it.toTrack(message, title)
            }
        }
    }
}

data class Args(
    val count: Int?,
    val favourites: Boolean,
    val shuffled: Boolean,
)

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

    init {
        LavaPlayerManager.registerAllSources()
    }

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
        loaders: List<TrackLoader>
    ): Int {
        val guildId = event.guildId ?: return 0
        val channel = event.member?.getVoiceStateOrNull()?.getChannelOrNull() ?: return 0
        val session = sessions[guildId] ?: connect(channel)

        var queueSize = session.queue.size
        if (session.current == null) {
            queueSize--
        }

        for (loader in loaders) {
            loader.invoke()?.let {
                session.queue.add(it)
            } ?: event.replyWith("Loading tracks failed...")
        }

        return queueSize + loaders.size
    }

    private suspend fun queueTracksByLink(
        event: MessageCreateEvent,
        link: String,
        count: Int = 1,
        shuffled: Boolean = false
    ) {
        var tracks = Track.playlistLoader(event.message, link).invoke()
        if (shuffled) {
            tracks = tracks.shuffled()
        }

        if (tracks.isEmpty()) {
            event.replyWith("No such track or playlist was found :(")
            return
        }

        val loaders = tracks.map { it.toLoader() }
        val queueSize = addTracksToQueue(event, loaders.repeatNTimes(count))

        if (queueSize > 0) {
            val msg = if (tracks.size > 1) {
                if (count == 1) {
                    "queued playlist with ${tracks.size} tracks"
                } else {
                    "queued playlist with ${tracks.size} tracks $count times"
                }
            } else {
                val title = tracks.first().title
                if (count == 1) {
                    "queued track: $title"
                } else {
                    "queued $count tracks: $title"
                }
            }

            event.replyWith(msg)
        }
    }

    private suspend fun queueTrackBySearch(
        event: MessageCreateEvent,
        query: String,
        count: Int = 1
    ) {
        val track = Track.trackLoader(event.message, "ytsearch: $query").invoke() ?: run {
            event.replyWith("No such track was found :(")
            return
        }

        val queueSize = addTracksToQueue(event, track.toLoader().repeatNTimes(count))

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

        val track = Track.trackLoader(event.message, file.absolutePath, query).invoke() ?: run {
            event.replyWith("Couldn't load tts :(")
            return
        }

        session.ttsQueue.add(track)
    }

    suspend fun play(event: MessageCreateEvent, count: Int = 1) {
        val query = event.query
        if (query.isBlank()) return

        if (Regex("https?://.+").matches(query)) {
            queueTracksByLink(event, query, count)
        } else {
            queueTrackBySearch(event, query, count)
        }
    }

    suspend fun stop(event: MessageCreateEvent) {
        val guildId = event.guildId ?: return
        disconnect(guildId)
    }

    private fun parseArgs(event: MessageCreateEvent): Args {
        var count: Int? = null
        var favourites = true
        var shuffled = false

        for (arg in event.query.split(" ")) {
            when (arg) {
                "-s", "--shuffle", "shuffle", "--shuffled", "shuffled" -> shuffled = true
                "-a", "--all", "all", "--full", "full" -> favourites = false
                "-as", "-sa" -> {
                    shuffled = true
                    favourites = false
                }

                else -> {
                    count = arg.toIntOrNull()
                }
            }
        }

        return Args(count, favourites, shuffled)
    }

    private suspend fun loadFromRepo(
        event: MessageCreateEvent,
        repository: SongRepository,
        loadingString: String
    ) {
        val loading = event.replyWith("*$loadingString*")

        val args = parseArgs(event)

        val builders = repository.getBuilders(event.message, args.count, args.favourites, args.shuffled)
        addTracksToQueue(event, builders)

        loading.delete()
        if (connections.containsKey(event.guildId)) {
            queue(event)
        }
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

        val skipped = skippedTracks.joinToString(separator = "\n")
        event.replyWith("skipped:\n```$skipped\n".take(MAX_MESSAGE_LENGTH - 3) + "```")
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

    suspend fun pirat(event: MessageCreateEvent) {
        loadFromRepo(event, PiratRepository, "Добавляю серегу...")
    }

    suspend fun antihype(event: MessageCreateEvent) {
        loadFromRepo(event, AntihypeRepository, "Это ты зря другалёчек...")
    }

    suspend fun nemimohype(event: MessageCreateEvent) {
        loadFromRepo(event, NemimohypeRepository, "Ну да мы живём не мимо хайпа...")
    }

    suspend fun hypetrain(event: MessageCreateEvent) {
        loadFromRepo(event, HypeTrainRepository, "Добавляю поезд хайпа...")
    }

    suspend fun antihypetrain(event: MessageCreateEvent) {
        loadFromRepo(event, AntihypeTrainRepository, "Добавляю анти поезд хайпа...")
    }

    suspend fun zamay(event: MessageCreateEvent) {
        loadFromRepo(event, ZamayRepository, "Добавляю замая...")
    }

    suspend fun mrgaslight(event: MessageCreateEvent) {
        loadFromRepo(event, MrGaslightRepository, "Добавляю мистера газлайта...")
    }

    suspend fun lusthero3(event: MessageCreateEvent) {
        loadFromRepo(event, LustHero3Repository, "Добавляю героя похоти три...")
    }

    suspend fun slavakpss(event: MessageCreateEvent) {
        loadFromRepo(event, SlavaKPSSRepository, "Добавляю славу...")
    }

    suspend fun russianfield(event: MessageCreateEvent) {
        loadFromRepo(event, RussianFieldRepository, "Добавляю русское поле...")
    }

    suspend fun bootlegvolume1(event: MessageCreateEvent) {
        loadFromRepo(event, BootlegVolume1Repository, "Добавляю бутлег vol 1...")
    }

    suspend fun angelstrue(event: MessageCreateEvent) {
        loadFromRepo(event, AngelsTrueRepository, "Добавляю ангельское тру...")
    }

    private suspend fun loadTrackByLink(event: MessageCreateEvent, link: String) {
        queueTracksByLink(event, link, count = event.query.toIntOrNull() ?: 1)
    }

    suspend fun snus(event: MessageCreateEvent) {
        loadTrackByLink(event, "https://www.youtube.com/watch?v=mx-f_wbZTMI")
    }

    suspend fun pauk(event: MessageCreateEvent) {
        loadTrackByLink(event, "https://www.youtube.com/watch?v=e2RqDHziN6k")
    }

    suspend fun sasha(event: MessageCreateEvent) {
        loadTrackByLink(event, "https://www.youtube.com/watch?v=0vQBaqUPtlc")
    }

    private suspend fun loadPlaylistByLink(event: MessageCreateEvent, link: String) {
        val args = parseArgs(event)
        queueTracksByLink(event, link, count = args.count ?: 1, shuffled = args.shuffled)
    }

    suspend fun ruslan(event: MessageCreateEvent) {
        loadPlaylistByLink(event, "https://www.youtube.com/playlist?list=PLpXSZSgpFNH-GPpNp9S_76hJBVWxUXWIR")
    }

    suspend fun help(event: MessageCreateEvent) {
        event.replyWith("```То же самое можно прочитать на https://chimchima.ru/bot\n$USAGE```")
    }
}
