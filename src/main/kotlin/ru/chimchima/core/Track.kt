package ru.chimchima.core

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.entity.Message
import ru.chimchima.player.LavaPlayerManager
import ru.chimchima.utils.formatDuration

typealias TrackLoader = suspend () -> Track?
typealias PlaylistLoader = suspend () -> List<Track>

class Track(
    private val audioTrack: AudioTrack,
    val message: Message,
    val title: String
) {
    fun playWith(player: AudioPlayer) = player.playTrack(audioTrack)
    fun clone() = Track(audioTrack.makeClone(), message, title)

    fun seek(millis: Long) {
        val newPosition = audioTrack.position + millis
        audioTrack.position = newPosition.coerceIn(0..audioTrack.duration)
    }

    fun startOver() {
        audioTrack.position = 0
    }

    companion object {
        private fun AudioTrack.toTrack(message: Message, title: String? = null): Track {
            val fullTitle = "${title ?: info.title} ${formatDuration()}"
            return Track(this, message, fullTitle)
        }

        suspend fun trackLoader(message: Message, query: String, title: String? = null): TrackLoader = {
            LavaPlayerManager.loadTrack(query)?.toTrack(message, title)
        }

        suspend fun playlistLoader(message: Message, query: String, title: String? = null): List<Track> {
            return LavaPlayerManager.loadPlaylist(query).map {
                it.toTrack(message, title)
            }
        }
    }
}
