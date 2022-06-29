package ru.chimchima.properties

import java.util.*

private const val LOCAL_PROPERTIES = "/local.properties"
const val DISCORD_TOKEN = "DISCORD_TOKEN"

object LocalProperties {
    private val properties = Properties().apply {
        LocalProperties::class.java.getResourceAsStream(LOCAL_PROPERTIES)?.let {
            load(it)
        }
    }

    val discordToken: String?
        get() = properties.getProperty(DISCORD_TOKEN) ?: System.getenv(DISCORD_TOKEN)
}
