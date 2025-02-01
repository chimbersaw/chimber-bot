package ru.chimchima.utils

import java.util.*

private const val LOCAL_PROPERTIES_FILE = "/local.properties"

object LocalProperties {
    private val properties = Properties().apply {
        LocalProperties::class.java.getResourceAsStream(LOCAL_PROPERTIES_FILE)?.let {
            load(it)
        }
    }

    private fun getProperty(name: String): String? {
        return System.getProperty(name) ?: System.getenv(name) ?: properties.getProperty(name)
    }

    val discordToken: String?
        get() = getProperty("DISCORD_TOKEN")

    val port: Int?
        get() = getProperty("server.port")?.toIntOrNull()

    val yaOauthToken: String?
        get() = getProperty("YANDEX_OAUTH_TOKEN")

    val yaFolderId: String?
        get() = getProperty("YANDEX_FOLDER_ID")

    val youtubeRefreshToken: String?
        get() = getProperty("YOUTUBE_REFRESH_TOKEN")

    val youtubePoToken: String?
        get() = getProperty("YOUTUBE_PO_TOKEN")

    val youtubeVisitorData: String?
        get() = getProperty("YOUTUBE_VISITOR_DATA")
}
