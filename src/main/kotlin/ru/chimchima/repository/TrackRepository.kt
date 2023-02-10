package ru.chimchima.repository

import dev.kord.core.entity.Message
import ru.chimchima.Track

data class Song(
    val title: String,
    val url: String,
    val favourite: Boolean = true
)

fun List<Pair<String, String>>.toSongs() = map { Song(it.first, it.second) }
fun List<Pair<Pair<String, String>, Boolean>>.toSongs() = map { Song(it.first.first, it.first.second, it.second) }

abstract class SongRepository {
    protected abstract val songs: List<Song>

    suspend fun getBuilders(
        message: Message,
        count: Int?,
        favourite: Boolean,
        shuffled: Boolean = false
    ): List<suspend () -> Track?> {
        var songsList = if (shuffled) songs.shuffled() else songs
        songsList = songsList.take(count ?: songsList.size)
        if (favourite) {
            songsList = songsList.filter { it.favourite }
        }

        return songsList.map { (title, url) ->
            Track.builder(message, url, title)
        }
    }
}
