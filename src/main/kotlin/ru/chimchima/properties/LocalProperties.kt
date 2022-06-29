package ru.chimchima.properties

import java.util.*

const val DISCORD_TOKEN = "DISCORD_TOKEN"
private const val PORT = "server.port"
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
}
