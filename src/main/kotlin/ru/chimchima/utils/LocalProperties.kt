package ru.chimchima.utils

import java.util.*

const val DISCORD_TOKEN = "DISCORD_TOKEN"
private const val PORT = "server.port"
private const val YANDEX_OAUTH_TOKEN = "YANDEX_OAUTH_TOKEN"
private const val YANDEX_FOLDER_ID = "YANDEX_FOLDER_ID"
private const val HEROKU_TOKEN = "HEROKU_TOKEN"
private const val LOCAL_PROPERTIES = "/local.properties"

object LocalProperties {
    private val properties = Properties().apply {
        LocalProperties::class.java.getResourceAsStream(LOCAL_PROPERTIES)?.let {
            load(it)
        }
    }

    private fun getProperty(name: String): String? {
        return System.getProperty(name) ?: System.getenv(name) ?: properties.getProperty(name)
    }

    val discordToken: String?
        get() = getProperty(DISCORD_TOKEN)

    val port: Int?
        get() = getProperty(PORT)?.toIntOrNull()

    val yaOauthToken: String?
        get() = getProperty(YANDEX_OAUTH_TOKEN)

    val yaFolderId: String?
        get() = getProperty(YANDEX_FOLDER_ID)

    val herokuToken: String?
        get() = getProperty(HEROKU_TOKEN)
}
