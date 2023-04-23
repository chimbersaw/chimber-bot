package ru.chimchima.repository

import dev.kord.core.entity.Message
import ru.chimchima.Track
import ru.chimchima.TrackLoader
import ru.chimchima.utils.repeatNTimes

data class Song(
    val title: String,
    val url: String,
    val favourite: Boolean = false
)

fun List<Pair<String, String>>.toFavouriteSongs() = map { Song(it.first, it.second, true) }
fun List<Pair<Pair<String, String>, Boolean>>.toSongs() = map { Song(it.first.first, it.first.second, it.second) }

abstract class SongRepository {
    abstract val songs: List<Song>

    suspend fun getLoaders(
        message: Message,
        limit: Int?,
        count: Int?,
        favourites: Boolean = true,
        shuffled: Boolean = false
    ): List<TrackLoader> {
        var songsList = if (shuffled) songs.shuffled() else songs
        if (favourites) {
            songsList = songsList.filter { it.favourite }
        }
        songsList = songsList.repeatNTimes(count ?: 1)
        songsList = songsList.take(limit ?: songsList.size)

        return songsList.map { (title, url) ->
            Track.trackLoader(message, url, title)
        }
    }
}
