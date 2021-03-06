package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.Permission
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.servers.Giveaways
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import org.jetbrains.exposed.sql.and

class GiveawayRerollCommand : LorittaDiscordCommand(arrayOf("giveaway reroll", "sorteio reroll"), CommandCategory.FUN) {
	companion object {
		const val LOCALE_PREFIX = "commands.fun.giveawayreroll"
	}

	override val discordPermissions = listOf(
            Permission.MESSAGE_MANAGE
    )

    override val canUseInPrivateChannel = false

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.fun.giveawayreroll.description"]
    }

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale, args: Array<String>) {
		val link = context.args.getOrNull(0)

		if (link == null) {
			context.explain()
			return
		}

		val split = link.split("/")

		var messageId: Long? = null
		var channelId: Long? = null

		if (split.size == 1 && split[0].isValidSnowflake()) {
			messageId = split[0].toLong()
		} else {
			messageId = split.getOrNull(split.size - 1)?.toLongOrNull()
			channelId = split.getOrNull(split.size - 2)?.toLongOrNull()
		}

		if (messageId == null) {
			context.reply(
                    LorittaReply(
                            locale["${GiveawayEndCommand.LOCALE_PREFIX}.giveawayInvalidArguments", "`https://canary.discordapp.com/channels/297732013006389252/297732013006389252/594270558238146603`"],
                            Constants.ERROR
                    )
			)
			return
		}

		val giveaway = loritta.newSuspendedTransaction {
			if (channelId != null) {
				Giveaway.find {
					(Giveaways.guildId eq context.guild!!.id) and (Giveaways.messageId eq messageId) and (Giveaways.textChannelId eq channelId)
				}.firstOrNull()
			} else {
				Giveaway.find {
					(Giveaways.guildId eq context.guild!!.id) and (Giveaways.messageId eq messageId)
				}.firstOrNull()
			}
		}

		if (giveaway == null) {
			context.reply(
                    LorittaReply(
                            locale["${GiveawayEndCommand.LOCALE_PREFIX}.giveawayDoesNotExist"],
                            Emotes.LORI_HM
                    )
			)
			return
		}

		if (!giveaway.finished) {
			context.reply(
                    LorittaReply(
                            locale[
                                    "${LOCALE_PREFIX}.giveawayStillRunning",
                                    "`${locale["${LOCALE_PREFIX}.giveawayHowToEnd", context.config.commandPrefix, link.stripCodeMarks()]}`"
                            ],
                            Constants.ERROR
                    )
			)
			return
		}

		val textChannel = context.discordGuild!!.getTextChannelById(giveaway.textChannelId)

		if (textChannel == null) {
			context.reply(
                    LorittaReply(
                            locale["${GiveawayEndCommand.LOCALE_PREFIX}.channelDoesNotExist"],
                            Constants.ERROR
                    )
			)
			return
		}
		val message = textChannel.retrieveMessageById(messageId).await()

		if (message == null) {
			context.reply(
                    LorittaReply(
                            locale["${GiveawayEndCommand.LOCALE_PREFIX}.messageDoesNotExist"],
                            Constants.ERROR
                    )
			)
			return
		}

		GiveawayManager.rollWinners(message, giveaway)

		context.reply(
                LorittaReply(
                        locale["${LOCALE_PREFIX}.rerolledGiveaway"],
                        Emotes.LORI_HAPPY
                )
		)
    }
}