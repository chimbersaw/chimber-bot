package ru.chimchima.repository

import dev.kord.core.entity.Message
import ru.chimchima.Track

data class Song(
    val title: String,
    val url: String,
    val favourite: Boolean = false
)

fun List<Pair<String, String>>.toFavouriteSongs() = map { Song(it.first, it.second, true) }
fun List<Pair<Pair<String, String>, Boolean>>.toSongs() = map { Song(it.first.first, it.first.second, it.second) }

abstract class SongRepository {
    abstract val songs: List<Song>

    suspend fun getBuilders(
        message: Message,
        count: Int?,
        favourite: Boolean = true,
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
