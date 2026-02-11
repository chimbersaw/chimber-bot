package ru.chimchima.core

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.entity.Message
import ru.chimchima.player.LavaPlayerManager
import ru.chimchima.utils.formatTime
import ru.chimchima.utils.generateStatusBar

class TrackLoader(val query: String, private val lambda: suspend () -> Track?) {
    suspend fun invoke() = lambda.invoke()
}

class Track(
    private val audioTrack: AudioTrack,
    val message: Message?,
    val title: String
) {
    val uri: String? = audioTrack.info.uri

    fun playWith(player: AudioPlayer) = player.playTrack(audioTrack)
    fun clone() = Track(audioTrack.makeClone(), message, title)

    fun statusBar(): String {
        return generateStatusBar(audioTrack.position, audioTrack.duration)
    }

    fun seek(millis: Long) {
        val newPosition = audioTrack.position + millis
        audioTrack.position = newPosition.coerceIn(0..audioTrack.duration)
    }

    fun startOver() {
        audioTrack.position = 0
    }

    companion object {
        private fun AudioTrack.toTrack(message: Message?, title: String? = null): Track {
            val fullTitle = "${title ?: info.title} ${formatTime(duration)}"
            return Track(this, message, fullTitle)
        }

        suspend fun trackLoader(message: Message?, query: String, title: String? = null) = TrackLoader(query) {
            LavaPlayerManager.loadTrack(query)?.toTrack(message, title)
        }

        suspend fun playlistLoader(message: Message?, query: String, title: String? = null): List<Track> {
            return LavaPlayerManager.loadPlaylist(query).map {
                it.toTrack(message, title)
            }
        }
    }
}
