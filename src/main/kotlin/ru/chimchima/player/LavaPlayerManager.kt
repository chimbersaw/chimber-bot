package ru.chimchima.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.lavalink.youtube.YoutubeAudioSourceManager
import kotlinx.coroutines.runBlocking
import ru.chimchima.heroku.HerokuClient
import ru.chimchima.utils.LocalProperties
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LavaPlayerManager : DefaultAudioPlayerManager() {
    fun registerAllSources() {
        AudioSourceManagers.registerLocalSource(this)

        // Register remote sources including `https://github.com/lavalink-devs/youtube-source#v2`.
        val youtube = YoutubeAudioSourceManager()
        youtube.useOauth2(LocalProperties.youtubeRefreshToken, true)
        registerSourceManager(youtube)

        registerSourceManager(YandexMusicAudioSourceManager())
        registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        registerSourceManager(BandcampAudioSourceManager())
        registerSourceManager(VimeoAudioSourceManager())
        registerSourceManager(TwitchStreamAudioSourceManager())
        registerSourceManager(BeamAudioSourceManager())
        registerSourceManager(GetyarnAudioSourceManager())
        registerSourceManager(NicoAudioSourceManager())
        registerSourceManager(HttpAudioSourceManager())

        val player = createPlayer().apply {
            addListener {
                if (it is TrackExceptionEvent) {
                    HerokuClient().restart()
                }
            }
        }

        // YouTube cache warming on start to speed up loading the first track
        runBlocking {
            player.playTrack(
                loadTrack("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            )
        }
    }

    suspend fun loadPlaylist(query: String): List<AudioTrack> = suspendCoroutine {
        loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = it.resume(listOf(track))
            override fun playlistLoaded(playlist: AudioPlaylist) = it.resume(playlist.tracks)
            override fun noMatches() = it.resume(emptyList())
            override fun loadFailed(exception: FriendlyException) {
                println("Load failed. Query: $query, reason: ${exception.stackTraceToString()}")
                it.resume(emptyList())
            }
        })
    }

    suspend fun loadTrack(query: String): AudioTrack? = loadPlaylist(query).firstOrNull()
}
