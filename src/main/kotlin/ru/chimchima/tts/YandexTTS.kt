package ru.chimchima.tts

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import ru.chimchima.properties.LocalProperties
import ru.chimchima.utils.runOnSuccessOrNull
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

const val YANDEX_TTS_URL = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize"
const val YANDEX_IAM_TOKEN_URL = "https://iam.api.cloud.yandex.net/iam/v1/tokens"

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
    private val oauthToken = LocalProperties.oauthToken
    private val folderId = LocalProperties.folderId
    private val client = HttpClient()
    private var iamToken = requestIamToken()

    private fun requestIamToken(): String? = runBlocking {
        val response = client.post(YANDEX_IAM_TOKEN_URL) {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("yandexPassportOauthToken", JsonPrimitive(oauthToken))
                }.toString()
            )
        }

        response.runOnSuccessOrNull { body: String ->
            val json: JsonObject = Json.decodeFromString(body)
            json.getValue("iamToken").jsonPrimitive.content
        }
    }

    init {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            iamToken = requestIamToken()
        }, 1, 1, TimeUnit.HOURS)
    }

    suspend fun textToAudioFile(text: String, file: File, jane: Boolean = false): Boolean {
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

        val response = client.submitForm(
            YANDEX_TTS_URL,
            parametersOf("text", text)
        ) {
            bearerAuth(iamToken ?: "")

            parameter("folderId", folderId)
            parameter("lang", lang)
            parameter("voice", voice)
            parameter("emotion", emotion)
        }

        return response.runOnSuccessOrNull { body: ByteArray ->
            file.writeBytes(body)
            true
        } ?: false
    }
}
