package ru.chimchima.repository

data class Song(
    val title: String,
    val url: String
)

abstract class SongRepository {
    protected abstract val songs: List<Song>

    fun getSongs(count: Int?, shuffled: Boolean = false): List<Song> {
        val songsList = if (shuffled) songs.shuffled() else songs
        return songsList.take(count ?: songsList.size)
    }
}
