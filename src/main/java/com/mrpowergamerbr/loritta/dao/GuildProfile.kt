package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.userdata.MongoLorittaProfile
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class GuildProfile(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<GuildProfile>(GuildProfiles)

	var guildId by GuildProfiles.guildId
	var userId by GuildProfiles.userId
	var xp by GuildProfiles.xp
	var quickPunishment by GuildProfiles.quickPunishment
	var money by GuildProfiles.money

	fun getCurrentLevel(): MongoLorittaProfile.XpWrapper {
		return MongoLorittaProfile.XpWrapper((xp / 1000).toInt(), xp)
	}

	fun getExpToAdvanceFrom(lvl: Int): Int {
		return lvl * 1000
	}
}