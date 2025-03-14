package ru.chimchima.core

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent

private val SASHA_DAUN = listOf("!rep", "!again", "!yadaun")
private val ALLOW_NEGATIVE_COUNT = listOf("!seek", "!ff")
private val ALLOW_OVERRIDE_COUNT = Regex("(!play|!next|!force)\\d*")

class Command private constructor(
    val name: String,
    val message: Message?,
    val args: Args,
    val guildId: Snowflake,
    val member: Member
) {
    val isPlay: Boolean
        get() = name matches ALLOW_OVERRIDE_COUNT

    companion object {
        fun create(event: MessageCreateEvent, prevCommand: Command? = null): Command? {
            var name = event.message.content.substringBefore(" ").lowercase()
            if (name in SASHA_DAUN) {
                return prevCommand
            }

            val guildId = event.guildId ?: return null
            val member = event.member ?: return null
            val args = Args.parse(event, name in ALLOW_NEGATIVE_COUNT)

            if (name matches ALLOW_OVERRIDE_COUNT) {
                args.count = name.dropWhile { !it.isDigit() }.toIntOrNull()
                name = name.takeWhile { !it.isDigit() }
            }

            return Command(name, event.message, args, guildId, member)
        }

        fun empty(guildId: Snowflake, member: Member) = Command("", null, Args.default(), guildId, member)
    }
}
