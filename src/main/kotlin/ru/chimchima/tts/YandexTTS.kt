package ru.chimchima.tts

import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import ru.chimchima.properties.LocalProperties
import java.io.File

const val YANDEX_TTS_URL = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize"

class YandexTTS : TextToSpeech {
    private val iamToken = LocalProperties.iamToken
    private val folderId = LocalProperties.folderId

    override fun textToAudioFile(text: String, file: File): Boolean {
        if (iamToken == null || folderId == null) return false

        val params = listOf(
            "lang" to "ru-RU",
            "voice" to "filipp",
            "folderId" to folderId
        )

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
