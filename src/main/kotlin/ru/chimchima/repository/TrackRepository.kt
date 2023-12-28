package ru.chimchima.repository

import dev.kord.core.entity.Message
import ru.chimchima.core.Args
import ru.chimchima.core.Track
import ru.chimchima.core.TrackLoader

data class Song(
    val title: String,
    val url: String,
    val favourite: Boolean = false
)

infix fun String.fav(url: String): Song = Song(this, url, favourite = true)
infix fun String.kal(url: String): Song = Song(this, url, favourite = false)

interface SongRepository {
    val songs: List<Song>

    suspend fun getLoaders(message: Message?, args: Args): List<TrackLoader> {
        return args.applyToSongList(songs).map { (title, url) ->
            Track.trackLoader(message, url, title)
        }
    }
}
