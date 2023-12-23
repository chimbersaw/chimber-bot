package ru.chimchima.heroku

import io.ktor.client.*

private const val HEROKU_CHIMBER_BOT_APP = "https://api.heroku.com/apps/chimber-bot/dynos"

class HerokuClient {
    private val client = HttpClient()

    fun restart() {

    }
}
