package ru.chimchima.core

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import ru.chimchima.player.LavaPlayerManager
import ru.chimchima.repository.*
import ru.chimchima.tts.TTSManager
import ru.chimchima.utils.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val MAX_MESSAGE_LENGTH = 2000

data class Session(
    val player: AudioPlayer,
    var queue: LinkedBlockingDeque<Track>,
    var ttsQueue: LinkedBlockingDeque<Track>,
    val channel: BaseVoiceChannelBehavior,
    var current: Track? = null
)

data class SessionConfig(
    var pause: Boolean = false,
    var repeat: Boolean = false,
    var stay: Boolean = false
)

@OptIn(KordVoice::class)
class ChimberCommands {
    private val ttsManager = TTSManager()
    private val messageHandler = MessageHandler()

    private val connections = ConcurrentHashMap<Snowflake, VoiceConnection>()
    private val sessions = ConcurrentHashMap<Snowflake, Session>()
    private val configs = ConcurrentHashMap<Snowflake, SessionConfig>()

    init {
        LavaPlayerManager.registerAllSources()
    }

    private suspend fun disconnect(guildId: Snowflake) {
        sessions.remove(guildId)?.let {
            it.current = null
            it.queue.clear()
            it.ttsQueue.clear()
            it.player.stopTrack()
        }

        configs.remove(guildId)
        connections.remove(guildId)?.shutdown()
    }

    private suspend fun connect(channel: BaseVoiceChannelBehavior): Session {
        val player = LavaPlayerManager.createPlayer()
        val queue = LinkedBlockingDeque<Track>()
        val ttsPlayer = LavaPlayerManager.createPlayer()
        val ttsQueue = LinkedBlockingDeque<Track>()

        val guildId = channel.guildId
        val session = Session(player, queue, ttsQueue, channel)
        val config = configs.computeIfAbsent(guildId) {
            SessionConfig()
        }

        // TODO: this lambda may be invoked before the queue is populated with a track leading to a disconnect.
        val connection = channel.connect {
            audioProvider {
                ttsPlayer.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }

                session.ttsQueue.poll()?.let {
                    it.playWith(ttsPlayer)
                    return@audioProvider AudioFrame.SILENCE
                }

                if (config.pause) {
                    return@audioProvider AudioFrame.SILENCE
                }

                player.provide(1, TimeUnit.SECONDS)?.let {
                    return@audioProvider AudioFrame.fromData(it.data)
                }

                if (!config.repeat || session.current == null) {
                    session.current = session.queue.poll()?.also {
                        messageHandler.replyWith(it.message, "playing track: ${it.title}")
                    }
                } else {
                    session.current = session.current?.clone()
                }

                val track = session.current

                if (track == null && !config.stay) {
                    disconnect(guildId)
                    return@audioProvider null
                }

                track?.playWith(player)
                return@audioProvider AudioFrame.SILENCE
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            while (sessions.containsKey(guildId) && channel.voiceStates.count() > 1) {
                delay(10.minutes)
            }

            disconnect(guildId)
        }

        sessions[guildId] = session
        connections[guildId] = connection

        return session
    }

    private suspend fun addTracksToQueue(command: Command, loaders: List<TrackLoader>): Int {
        val channel = command.member.getVoiceStateOrNull()?.getChannelOrNull() ?: return 0
        val session = sessions[command.guildId] ?: connect(channel)

        var queueSize = session.queue.size
        if (session.current == null) {
            queueSize--
        }

        val playNext = command.args.playNext
        val orderedLoaders = if (command.args.playNext) loaders.reversed() else loaders
        for (loader in orderedLoaders) {
            loader.invoke()?.let {
                if (playNext) {
                    session.queue.addFirst(it.clone())
                } else {
                    session.queue.addLast(it.clone())
                }
            } ?: messageHandler.replyWith(command, "Track `${loader.query}` failed to load, skipping...")
        }

        return queueSize + loaders.size
    }

    private suspend fun queueTracksByLink(command: Command, link: String) {
        val tracks = Track.playlistLoader(command.message, link)

        if (tracks.isEmpty()) {
            messageHandler.replyWith(command, "No such track or playlist was found :(")
            return
        }

        val args = command.args
        val loaders = args.applyToList(tracks).map { it.toLoader() }
        if (loaders.isEmpty()) {
            return
        }

        val queueSize = addTracksToQueue(command, loaders)
        if (queueSize == 0) {
            return
        }

        val count = args.count ?: 1
        val msg = if (tracks.size > 1) {
            if (count == 1) {
                "queued playlist with ${loaders.size} tracks"
            } else if (args.limit == null) {
                "queued playlist with ${tracks.size} tracks $count times"
            } else {
                "queued playlist with ${tracks.size} tracks $count times limited to ${loaders.size}"
            }
        } else {
            val title = tracks.first().title
            if (count == 1) {
                "queued track: $title"
            } else {
                "queued ${loaders.size} tracks: $title"
            }
        }

        messageHandler.replyWith(command, msg)
        if (tracks.size > 1) {
            queue(command)
        }
    }

    private suspend fun queueTrackBySearch(command: Command, query: String) {
        val track = Track.trackLoader(command.message, "ytsearch: $query").invoke() ?: run {
            messageHandler.replyWith(command, "No such track was found :(")
            return
        }

        val count = command.args.count ?: 1

        val queueSize = addTracksToQueue(command, track.toLoader().repeatNTimes(count))
        if (queueSize == 0) {
            return
        }

        val msg = if (count == 1) {
            "queued track: ${track.title}"
        } else {
            "queued $count tracks: ${track.title}"
        }

        messageHandler.replyWith(command, msg)
    }

    private suspend fun textToSpeech(command: Command, query: String, jane: Boolean = false) {
        val file = ttsManager.textToSpeech(query, jane) ?: run {
            messageHandler.replyWith(command, "Could not load tts :(")
            return
        }

        val channel = command.member.getVoiceStateOrNull()?.getChannelOrNull() ?: return
        val session = sessions[command.guildId] ?: connect(channel)

        val track = Track.trackLoader(command.message, file.absolutePath, query).invoke() ?: run {
            messageHandler.replyWith(command, "Couldn't load tts :(")
            return
        }

        session.ttsQueue.addLast(track)
    }

    suspend fun say(command: Command, jane: Boolean = false) {
        val query = command.query
        if (query.isBlank()) return
        if (query.length > 500) {
            messageHandler.replyWith(command, "!say query must be no longer than 500 symbols")
            return
        }

        textToSpeech(command, query, jane)
    }

    suspend fun plink(command: Command) {
        val message = command.message
        val response = messageHandler.replyWith(message, "plonk!", true)

        delay(5000)
        messageHandler.delete(message)
        messageHandler.delete(response)
    }

    suspend fun play(command: Command) {
        val query = command.query
        if (query.isBlank()) return

        if (query.isHttp()) {
            queueTracksByLink(command, query)
        } else {
            queueTrackBySearch(command, query)
        }
    }

    suspend fun stop(command: Command) {
        disconnect(command.guildId)
    }

    private suspend fun loadFromRepo(
        command: Command,
        repository: SongRepository,
        loadingString: String
    ) {
        val loading = messageHandler.replyWith(command, "*$loadingString*")

        val loaders = repository.getLoaders(command.message, command.args)
        addTracksToQueue(command, loaders)

        messageHandler.delete(loading)

        queue(command)
    }

    suspend fun shuffle(command: Command) {
        val session = sessions[command.guildId] ?: run {
            messageHandler.replyWith(command, "Nothing to shuffle (daun).")
            return
        }

        val loading = messageHandler.replyWith(command, "*Shuffling...*")

        val shuffled = session.queue.shuffled()
        session.queue = LinkedBlockingDeque<Track>(shuffled)

        messageHandler.delete(loading)

        queue(command)
    }

    suspend fun reverse(command: Command) {
        val session = sessions[command.guildId] ?: run {
            messageHandler.replyWith(command, "Nothing to reverse (daun).")
            return
        }

        val loading = messageHandler.replyWith(command, "*Reversing...*")

        val reversed = session.queue.reversed()
        session.queue = LinkedBlockingDeque<Track>(reversed)

        messageHandler.delete(loading)

        queue(command)
    }

    suspend fun skip(command: Command) {
        val args = command.args
        val count = args.count ?: 1
        if (count < 1) return

        val (player, queue, _, _, current) = sessions[command.guildId] ?: return
        if (current == null) return

        val skippedTracks = mutableListOf(current.title)
        repeat(count - 1) {
            val track = queue.poll() ?: return@repeat
            skippedTracks.add(track.title)
        }
        player.stopTrack()
        sessions[command.guildId]?.current = null

        val skipped = skippedTracks.joinToString(separator = "\n")
        messageHandler.replyWith(command, "skipped:\n```$skipped\n".take(MAX_MESSAGE_LENGTH - 3) + "```")
    }

    suspend fun next(command: Command) {
        if (command.content.isBlank()) {
            skip(command)
        } else {
            command.args.playNext = true
            play(command)
        }
    }

    fun seek(command: Command) {
        val current = sessions[command.guildId]?.current ?: return
        val value = command.args.count ?: 10

        current.seek(value.seconds.inWholeMilliseconds)
    }

    fun back(command: Command) {
        val current = sessions[command.guildId]?.current ?: return
        current.startOver()
    }

    suspend fun queue(command: Command, forcedMessage: Boolean = false) {
        val queue = sessions[command.guildId]?.queue

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

        messageHandler.replyWith(command, reply, forcedMessage)
    }

    suspend fun clear(command: Command) {
        val queue = sessions[command.guildId]?.queue ?: run {
            messageHandler.replyWith(command, "Queue is already empty.")
            return
        }

        queue.clear()
        messageHandler.replyWith(command, "Queue cleared.")
    }

    suspend fun mute(command: Command) {
        messageHandler.mute(command)
    }

    suspend fun current(command: Command) {
        val track = sessions[command.guildId]?.current ?: return
        val title = track.title.substringBeforeLast(" ")
        val statusBar = track.statusBar()
        messageHandler.replyWith(command, "$title\n$statusBar", forcedMessage = true)
    }

    suspend fun status(command: Command) {
        current(command)
        queue(command, forcedMessage = true)
    }

    suspend fun repeat(command: Command) {
        val config = configs.computeIfAbsent(command.guildId) {
            SessionConfig()
        }

        var start = "Repeat is now"
        when (command.content.lowercase()) {
            "on", "1" -> config.repeat = true
            "off", "0" -> config.repeat = false
            else -> start = "Repeat is"
        }

        val mode = if (config.repeat) "on" else "off"
        messageHandler.replyWith(command, "$start $mode.")
    }

    suspend fun pause(command: Command) {
        configs[command.guildId]?.pause = true
        messageHandler.replyWith(command, "Player is paused.")
    }

    suspend fun resume(command: Command) {
        configs[command.guildId]?.pause = false
        messageHandler.replyWith(command, "Player is resumed.")
    }

    suspend fun stay(command: Command) {
        val config = configs.computeIfAbsent(command.guildId) {
            SessionConfig()
        }

        var start = "Stay is now"
        when (command.content.lowercase()) {
            "on", "1" -> config.stay = true
            "off", "0" -> config.stay = false
            else -> start = "Stay is"
        }

        val mode = if (config.stay) "on" else "off"
        messageHandler.replyWith(command, "$start $mode.")
    }

    suspend fun join(command: Command) {
        configs[command.guildId] = SessionConfig().apply { stay = true }
        addTracksToQueue(command, emptyList())
    }


    suspend fun onVoiceStateUpdate(event: VoiceStateUpdateEvent) {
        if (event.old?.channelId == event.state.channelId) return
        val member = event.state.getMemberOrNull() ?: return
        val channel = member.getVoiceStateOrNull()?.getChannelOrNull() ?: return
        val curChannel = sessions[member.guildId]?.channel
        if (curChannel != null && curChannel != channel) return

        val query = when (member.username) {
            "scanhex" -> "вот и нахуй ты зашел сашка"
            "andrbrawls" -> "всем привет с вами я - богдан т+ечис"
            "zot9" -> "всем привет с вами я мистер зота ак+а пожилая барракуда"
            "karburator14" -> "старый бог тут"
            else -> return
        }

        delay(1.seconds)
        val command = Command.empty(member.guildId, member)
        textToSpeech(command, query)
    }


    suspend fun pirat(command: Command) {
        loadFromRepo(command, PiratRepository, "О как же хорошо: моя чимчима не в курсе")
    }

    suspend fun cover(command: Command) {
        loadFromRepo(command, PiratCoverRepository, "Мне люди должны сказать: \"чимчима\"")
    }


    suspend fun antihype(command: Command) {
        loadFromRepo(command, AntihypeRepository, "Это ты зря другалёчек...")
    }

    suspend fun nemimohype(command: Command) {
        loadFromRepo(command, NemimohypeRepository, "Чимчима чимчимовна надежда грайма")
    }

    suspend fun hypetrain(command: Command) {
        loadFromRepo(command, HypeTrainRepository, "Гоша Чимчимский - самый модный")
    }

    suspend fun antihypetrain(command: Command) {
        loadFromRepo(command, AntihypeTrainRepository, "Славян отрыгнул на биток, а я тут начимчлю")
    }


    suspend fun zamay(command: Command) {
        loadFromRepo(command, ZamayRepository, "Добавляю замая...")
    }

    suspend fun mrgaslight(command: Command) {
        loadFromRepo(command, MrGaslightRepository, "Чимчимы из подполья эти помни навеки")
    }

    suspend fun lusthero3(command: Command) {
        loadFromRepo(command, LustHero3Repository, "Меня ждут в городах чимчимы")
    }


    suspend fun slavakpss(command: Command) {
        loadFromRepo(command, SlavaKPSSRepository, "Добавляю славу...")
    }

    suspend fun russianfield(command: Command) {
        loadFromRepo(command, RussianFieldRepository, "Выйду в русское поле: немного почимчимлю и вернусь в неволю")
    }

    suspend fun bootlegvolume1(command: Command) {
        loadFromRepo(command, BootlegVolume1Repository, "Иду по пищевой цепи, пожирая чимчимы")
    }

    suspend fun angelstrue(command: Command) {
        loadFromRepo(command, AngelsTrueRepository, "Ты не французский рэп, хотя показывал чимчима-флоу")
    }


    suspend fun krovostok(command: Command) {
        loadFromRepo(command, KrovostokRepository, "Добавляю кровостiк...")
    }

    suspend fun bloodriver(command: Command) {
        loadFromRepo(command, BloodRiverRepository, "Чимчима рано ударила в голову")
    }

    suspend fun skvoznoe(command: Command) {
        loadFromRepo(command, SkvoznoeRepository, "Чимчиму в массы: и натуралу, и пидорасу")
    }

    suspend fun dumbbell(command: Command) {
        loadFromRepo(command, DumbbellRepository, "Cперва я почимчимлю чимчиму, отложив пекаль")
    }

    suspend fun studen(command: Command) {
        loadFromRepo(command, StudenRepository, "Чимчима всегда наполовину полна, всегда")
    }

    suspend fun lombard(command: Command) {
        loadFromRepo(command, LombardRepository, "И хуле, что он заложник, он чимчима, но он человек")
    }

    suspend fun cheburashka(command: Command) {
        loadFromRepo(command, CheburashkaRepository, "Передо мной в пакете Дикси лежит отрезанная чимчима́")
    }

    suspend fun nauka(command: Command) {
        loadFromRepo(command, NaukaRepository, "Или всё будет не так, и чимчима спиздила. Проверим.")
    }

    suspend fun krovonew(command: Command) {
        loadFromRepo(command, KrovostokMisc, "Бог оказался фраером, расчимчимленным фраерком")
    }


    suspend fun crystalcastles(command: Command) {
        loadFromRepo(command, CrystalCastlesRepository, "Кристальные чимчимы")
    }

    suspend fun cc1(command: Command) {
        loadFromRepo(command, CC1Repository, "Tell me what to chimchima")
    }

    suspend fun cc2(command: Command) {
        loadFromRepo(command, CC2Repository, "I am made of chimchima")
    }

    suspend fun cc3(command: Command) {
        loadFromRepo(command, CC3Repository, "Wrath of chimchima")
    }


    suspend fun snus(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=mx-f_wbZTMI")
    }

    suspend fun pauk(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=e2RqDHziN6k")
    }

    suspend fun sasha(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=0vQBaqUPtlc")
    }

    suspend fun discord(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=8tmxUJDjVhY")
    }

    suspend fun sperma(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=QcbGm8FsbZg")
    }

    suspend fun taxi(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=A010SCa1S8U")
    }

    suspend fun diss(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=QzrFC51rwDs")
    }

    suspend fun kokiki(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=aFt6Q4fxFqY")
    }

    suspend fun koshechki(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=hc5YkbnH2pY")
    }

    suspend fun satana(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=-hU2T7IXtH4")
    }

    suspend fun skibidi(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=ejda6cMXF2s")
    }

    suspend fun zhuravli(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=Lcli6EIECRw")
    }


    suspend fun valera(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=1uPYwaUZmH0")
    }

    suspend fun lowgrades(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/watch?v=8Fc1IvNu9a0")
    }

    suspend fun val0(command: Command) {
        loadFromRepo(command, ValRepository, "жопа")
    }

    suspend fun cocyxa(command: Command) {
        command.args.count = command.args.count ?: 5
        queueTracksByLink(
            command,
            "https://static.wikia.nocookie.net/dota2_ru_gamepedia/images/9/94/Snip_death_08_ru.mp3"
        )
    }

    suspend fun cocyxa2(command: Command) {
        val count = minOf(10, command.args.count ?: 5)
        repeat(count) {
            textToSpeech(command, "предсмертный выстрел")
        }
    }

    suspend fun raketa(command: Command) {
        command.args.count = command.args.count ?: 5
        queueTracksByLink(
            command,
            "https://static.wikia.nocookie.net/dota2_ru_gamepedia/images/3/38/Ratt_ability_flare_04_ru.mp3/revision/latest"
        )
    }


    suspend fun ruslan(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/playlist?list=PLpXSZSgpFNH-GPpNp9S_76hJBVWxUXWIR")
    }

    suspend fun vlad(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/playlist?list=PLpXSZSgpFNH-Tljl-1zF9B-JMMTxCTJlX")
    }

    suspend fun fallout(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/playlist?list=PL63B26E837C45A200")
    }

    suspend fun zov(command: Command) {
        queueTracksByLink(command, "https://www.youtube.com/playlist?list=PLznnQmzo0pMu7V_aL0E_wgIYbO2JK43PE")
    }


    suspend fun help(command: Command) {
        messageHandler.replyWith(command, "https://chimchima.ru/bot")
    }
}
