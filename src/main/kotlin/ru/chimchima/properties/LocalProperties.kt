package ru.chimchima.properties

import java.util.*

const val DISCORD_TOKEN = "DISCORD_TOKEN"
private const val PORT = "server.port"
private const val YANDEX_OAUTH_TOKEN = "YANDEX_OAUTH_TOKEN"
private const val YANDEX_FOLDER_ID = "YANDEX_FOLDER_ID"
private const val LOCAL_PROPERTIES = "/local.properties"

object LocalProperties {
    private val properties = Properties().apply {
        LocalProperties::class.java.getResourceAsStream(LOCAL_PROPERTIES)?.let {
            load(it)
        }
    }

    private fun getProperty(name: String): String? {
        return properties.getProperty(name) ?: System.getProperty(name) ?: System.getenv(name)
    }

    val discordToken: String?
        get() = getProperty(DISCORD_TOKEN)

    val port: Int?
        get() = getProperty(PORT)?.toIntOrNull()

    val oauthToken: String?
        get() = getProperty(YANDEX_OAUTH_TOKEN)

    val folderId: String?
        get() = getProperty(YANDEX_FOLDER_ID)
}
