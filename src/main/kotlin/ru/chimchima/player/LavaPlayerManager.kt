package ru.chimchima.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
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
import dev.lavalink.youtube.clients.TvHtml5Embedded
import dev.lavalink.youtube.clients.Web
import kotlinx.coroutines.runBlocking
import ru.chimchima.utils.LocalProperties
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LavaPlayerManager : DefaultAudioPlayerManager() {
    fun registerAllSources() {
        AudioSourceManagers.registerLocalSource(this)

        // Register remote sources including `https://github.com/lavalink-devs/youtube-source#v2`.
        // TV client can load tracks by link or search (using oauth) but not playlists.
        val youtubeTv = YoutubeAudioSourceManager(TvHtml5Embedded())
        youtubeTv.useOauth2(LocalProperties.youtubeRefreshToken, true)

        // WEB client can load playlists but fails to play tracks.
        // Using poToken fixes the problem for some IPs/ASNs.
        val youtubeWeb = YoutubeAudioSourceManager(Web())
        Web.setPoTokenAndVisitorData(LocalProperties.youtubePoToken, LocalProperties.youtubeVisitorData)

        // TV client handles track loading, WEB client handles playlist loading (where TV client fails).
        registerSourceManager(youtubeTv)
        registerSourceManager(youtubeWeb)

        registerSourceManager(YandexMusicAudioSourceManager())
        registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        registerSourceManager(BandcampAudioSourceManager())
        registerSourceManager(VimeoAudioSourceManager())
        registerSourceManager(TwitchStreamAudioSourceManager())
        registerSourceManager(BeamAudioSourceManager())
        registerSourceManager(GetyarnAudioSourceManager())
        registerSourceManager(NicoAudioSourceManager())
        registerSourceManager(HttpAudioSourceManager())

        // YouTube cache warming on start to speed up loading the first track
        runBlocking {
            createPlayer().playTrack(
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
