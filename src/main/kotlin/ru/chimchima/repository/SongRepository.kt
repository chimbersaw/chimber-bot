package ru.chimchima.repository

data class Song(
    val title: String,
    val url: String
)

fun List<Pair<String, String>>.toSongs() = map { (title, url) ->
    Song(title, url)
}

abstract class SongRepository {
    protected abstract val songs: List<Song>

    fun getSongs(count: Int?, shuffled: Boolean = false): List<Song> {
        val songsList = if (shuffled) songs.shuffled() else songs
        return songsList.take(count ?: songsList.size)
    }
}
