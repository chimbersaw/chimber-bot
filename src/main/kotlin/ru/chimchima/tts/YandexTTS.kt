package ru.chimchima.tts

import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import ru.chimchima.properties.LocalProperties
import java.io.File

const val YANDEX_TTS_URL = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize"

private val enVoices = listOf("john" to null)
private val ruVoices = listOf(
    "alena" to "neutral",
    "alena" to "good",
    "filipp" to null,
    "ermil" to "neutral",
    "ermil" to "good",
    "jane" to "neutral",
    "jane" to "good",
    "jane" to "evil",
    "madirus" to null,
    "omazh" to "neutral",
    "omazh" to "evil",
    "zahar" to "neutral",
    "zahar" to "good"
)

class YandexTTS {
    private val iamToken = LocalProperties.iamToken
    private val folderId = LocalProperties.folderId

    fun textToAudioFile(text: String, file: File, jane: Boolean = false): Boolean {
        if (iamToken == null || folderId == null) return false

        val containsCyrillic = text.any { Character.UnicodeBlock.of(it) == Character.UnicodeBlock.CYRILLIC }
        val containsEnglish = text.any { it in 'a'..'z' || it in 'A'..'Z' }

        val (lang, voices) = if (containsEnglish && !containsCyrillic) {
            "en-US" to enVoices
        } else {
            "ru-RU" to ruVoices
        }

        val (voice, emotion) = if (jane) {
            "jane" to "evil"
        } else {
            voices.random()
        }

        val params = mutableListOf(
            "lang" to lang,
            "voice" to voice,
            "folderId" to folderId
        )

        emotion?.let {
            params.add("emotion" to it)
        }

        val (_, response, result) = YANDEX_TTS_URL.httpPost(params)
            .authentication().bearer(iamToken)
            .header("Content-Type" to "application/x-www-form-urlencoded")
            .body("text=$text")
            .response()

        return when (result) {
            is Result.Success -> {
                file.writeBytes(result.get())
                true
            }

            is Result.Failure -> {
                print(result.getException())
                println(response.body().asString("text/plain"))
                false
            }
        }
    }
}
