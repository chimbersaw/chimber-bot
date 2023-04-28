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

infix fun String.fav(url: String): Song = Song(this, url, favourite = true)
infix fun String.kal(url: String): Song = Song(this, url, favourite = false)

interface SongRepository {
    val songs: List<Song>

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
