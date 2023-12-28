package ru.chimchima.core

import dev.kord.core.event.message.MessageCreateEvent
import ru.chimchima.repository.Song
import ru.chimchima.utils.repeatNTimes
import ru.chimchima.utils.toSignedIntOrNull

class Args private constructor() {
    private var shuffled: Boolean = false
    private var favourites: Boolean = true
    var playNext: Boolean = false
    var count: Int? = null
    var limit: Int? = null

    fun <T> applyToList(list: List<T>): List<T> {
        // limit -> shuffle -> count
        var result = list.take(limit ?: list.size)
        if (shuffled) result = result.shuffled()
        return result.repeatNTimes(count ?: 1)
    }

    fun applyToSongList(songs: List<Song>): List<Song> {
        val favSongs = songs.filter { !favourites || it.favourite }
        return applyToList(favSongs)
    }

    private fun processShortArg(arg: String) {
        for (c in arg.drop(1)) {
            when (c) {
                's' -> shuffled = true
                'a' -> favourites = false
                'n' -> playNext = true
            }
        }
    }

    private fun processArgument(arg: String, allowNegative: Boolean = false) {
        when (arg) {
            "--shuffle", "shuffle", "--shuffled", "shuffled" -> shuffled = true
            "--all", "all", "--full", "full" -> favourites = false
            "--next", "next" -> playNext = true
        }

        if (arg.startsWith("-")) {
            processShortArg(arg)
        }

        if (arg.endsWith('l', ignoreCase = true)) {
            arg.dropLast(1).toSignedIntOrNull(allowNegative = false)?.let {
                limit = it
            }
        } else {
            arg.toSignedIntOrNull(allowNegative)?.let {
                count = it
            }
        }
    }

    companion object {
        fun parse(event: MessageCreateEvent, allowNegative: Boolean = false) = Args().apply {
            for (arg in event.message.content.split(" ").drop(1)) {
                processArgument(arg, allowNegative)
            }
        }

        fun default() = Args()
    }
}
