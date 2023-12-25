package ru.chimchima.heroku

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.chimchima.utils.LocalProperties
import ru.chimchima.utils.performRequest

private const val HEROKU_CHIMBER_BOT_DYNOS = "https://api.heroku.com/apps/chimber-bot/dynos"

class HerokuClient {
    private val client = HttpClient()
    private val token = LocalProperties.herokuToken.orEmpty()

    suspend fun restart(): Boolean {
        val response = client.delete(HEROKU_CHIMBER_BOT_DYNOS) {
            bearerAuth(token)
            accept(ContentType("application", "vnd.heroku+json; version=3"))
        }

        return response.performRequest()
    }
}
