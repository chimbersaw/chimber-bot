package ru.chimchima.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LavaPlayerManager : DefaultAudioPlayerManager() {
    fun registerAllSources() {
        AudioSourceManagers.registerRemoteSources(this)
        AudioSourceManagers.registerLocalSource(this)

        // YouTube cache warming on start to speed up loading the first track
        createPlayer().playTrack(
            runBlocking {
                loadTrack("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            }
        )
    }

    suspend fun loadTrack(query: String) = suspendCoroutine {
        loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = it.resume(track)
            override fun playlistLoaded(playlist: AudioPlaylist) = it.resume(playlist.tracks.first())
            override fun noMatches() = it.resume(null)
            override fun loadFailed(exception: FriendlyException?) = it.resume(null)
        })
    }
}
