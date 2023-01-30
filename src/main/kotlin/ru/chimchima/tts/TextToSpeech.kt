package ru.chimchima.tts

import java.io.File

interface TextToSpeech {
    fun textToAudioFile(text: String, file: File): Boolean
}
