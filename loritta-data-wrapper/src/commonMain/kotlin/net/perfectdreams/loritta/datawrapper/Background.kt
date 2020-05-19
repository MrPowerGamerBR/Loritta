package net.perfectdreams.loritta.datawrapper

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.api.utils.Rarity

@Serializable
class Background(
		val internalName: String,
		val imageFile: String,
		val enabled: Boolean,
		val rarity: Rarity,
		val createdBy: List<String>? = null,
		val crop: Crop? = null,
		val set: String? = null,
		val tag: String? = null
)