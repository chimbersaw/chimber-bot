package ru.chimchima.tts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration
import java.util.*

class TTSManager(private val tts: TextToSpeech) {
    private val tmp = File("tmp")

    init {
        tmp.mkdir()
        tmp.deleteOnExit()
    }

    fun textToSpeech(text: String): File? {
        val filename = "${UUID.randomUUID()}.ogg"
        val file = tmp.resolve(filename)
        file.deleteOnExit()

        CoroutineScope(Dispatchers.Default).launch {
            delay(Duration.ofMinutes(5))
            file.delete()
        }

        return if (tts.textToAudioFile(text, file)) {
            file
        } else {
            null
        }
    }
}
