package ru.chimchima.core

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent

private val SASHA_DAUN = listOf("!rep", "!again", "!yadaun")
private val ALLOW_NEGATIVE_COUNT = listOf("!seek", "!ff")
private val ALLOW_OVERRIDE_COUNT = listOf(Regex("!play\\d*"), Regex("!next\\d*"))

class Command private constructor(
    val name: String,
    val message: Message,
    val args: Args,
    val guildId: Snowflake,
    val member: Member
) {
    val isPlay: Boolean
        get() = ALLOW_OVERRIDE_COUNT.any { name matches it }

    companion object {
        fun create(event: MessageCreateEvent, prevCommand: Command? = null): Command? {
            var name = event.message.content.substringBefore(" ").lowercase()
            if (name in SASHA_DAUN) {
                return prevCommand
            }

            val guildId = event.guildId ?: return null
            val member = event.member ?: return null
            val args = Args.parse(event, name in ALLOW_NEGATIVE_COUNT)

            if (ALLOW_OVERRIDE_COUNT.any { name matches it }) {
                args.count = name.dropWhile { !it.isDigit() }.toIntOrNull()
                name = name.takeWhile { !it.isDigit() }
            }

            return Command(name, event.message, args, guildId, member)
        }
    }
}
