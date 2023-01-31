package ru.chimchima.tts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration
import java.util.*

class TTSManager {
    private val tts = YandexTTS()
    private val tmp = File("tmp")

    init {
        tmp.mkdir()
        tmp.deleteOnExit()
    }

    suspend fun textToSpeech(text: String, jane: Boolean = false): File? {
        val filename = "${UUID.randomUUID()}.ogg"
        val file = tmp.resolve(filename)
        file.deleteOnExit()

        CoroutineScope(Dispatchers.Default).launch {
            delay(Duration.ofMinutes(5))
            file.delete()
        }

        return if (tts.textToAudioFile(text, file, jane)) {
            file
        } else {
            null
        }
    }
}
