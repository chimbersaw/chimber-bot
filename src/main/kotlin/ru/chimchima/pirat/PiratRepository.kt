package ru.chimchima.pirat

import ru.chimchima.ChimberCommands

data class PiratSong(
    val title: String,
    val url: String
)

class PiratRepository {
    private val piratSongs = songs.mapIndexed { id, (title, youtubeUrl) ->
        val fileName = "/pirat/${id + 1}.mp3"
        val url = ChimberCommands::class.java.getResource(fileName)?.path ?: "ytsearch: $youtubeUrl"
        PiratSong(title, url)
    }

    fun getSongs(count: Int?, shuffled: Boolean = false): List<PiratSong> {
        val songsList = if (shuffled) piratSongs.shuffled() else piratSongs
        return songsList.take(count ?: songsList.size)
    }

    companion object {
        private val songs = listOf(
            "фп ам" to "https://youtu.be/FGeaG4eL4OY",
            "Тп на аме" to "https://youtu.be/ocRN6Hz3LrI",
            "Ну и что, что я вор?" to "https://youtu.be/KxpGxVQOkG0",
            "Мой байк" to "https://youtu.be/OQGdrezi0Y4",
            "Почему ты еще не фанат?" to "https://youtu.be/apWlgn-nrEE",
            "Свинья" to "https://youtu.be/w8TW3IkVfAQ",
            "zxcергей" to "https://youtu.be/-YAHPbTNBto",
            "Вайбмен" to "https://youtu.be/YpMU3g8JLjw",
            "Гимн Дахака" to "https://youtu.be/mZvEsykmfZY",
            "Апельсин" to "https://youtu.be/Vr-DOV-TP5s",
            "Солевар" to "https://youtu.be/KhX3T_NYndo",
            "Извини сегодня праздник" to "https://youtu.be/REReaqbM6Kw",
            "Я взлетаю вверх" to "https://youtu.be/oghlaYD2_FQ"
        )
    }
}
