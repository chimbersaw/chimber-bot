package ru.chimchima.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.runBlocking
import ru.chimchima.heroku.HerokuClient
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LavaPlayerManager : DefaultAudioPlayerManager() {
    fun registerAllSources() {
        AudioSourceManagers.registerRemoteSources(this)
        AudioSourceManagers.registerLocalSource(this)

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
            override fun loadFailed(exception: FriendlyException?) = it.resume(emptyList())
        })
    }

    suspend fun loadTrack(query: String): AudioTrack? = loadPlaylist(query).firstOrNull()
}
