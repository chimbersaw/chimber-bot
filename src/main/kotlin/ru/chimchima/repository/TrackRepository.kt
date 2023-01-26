package ru.chimchima.repository

import dev.kord.core.entity.Message
import ru.chimchima.Track

abstract class SongRepository {
    protected abstract val songs: List<Pair<String, String>>

    suspend fun getBuilders(message: Message, count: Int?, shuffled: Boolean = false): List<suspend () -> Track?> {
        val maybeShuffled = if (shuffled) songs.shuffled() else songs
        val songsList = maybeShuffled.take(count ?: maybeShuffled.size)

        return songsList.map { (title, url) ->
            Track.builder(message, url, title)
        }
    }
}
