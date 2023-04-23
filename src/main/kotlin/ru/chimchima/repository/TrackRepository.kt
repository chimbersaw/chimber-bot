package ru.chimchima.repository

import dev.kord.core.entity.Message
import ru.chimchima.Track
import ru.chimchima.TrackLoader

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
        favourites: Boolean = true,
        shuffled: Boolean = false
    ): List<TrackLoader> {
        var songsList = if (shuffled) songs.shuffled() else songs
        if (favourites) {
            songsList = songsList.filter { it.favourite }
        }
        songsList = songsList.take(count ?: songsList.size)

        return songsList.map { (title, url) ->
            Track.trackLoader(message, url, title)
        }
    }
}
