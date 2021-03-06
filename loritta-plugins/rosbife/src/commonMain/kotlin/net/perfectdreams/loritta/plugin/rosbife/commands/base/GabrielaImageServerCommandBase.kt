package net.perfectdreams.loritta.plugin.rosbife.commands.base

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes

/**
 * Commands that use Gabriela's Image Server Generator tool should use this class!
 *
 * TODO: This class could be multiplatform if the Base64 implementation was multiplatform!
 *
 * @param endpoint the page endpoint (example: "/api/v1/videos/carly-aaah")
 * @param fileName the sent file file name (example: "carly_aaah.mp4")
 */
abstract class GabrielaImageServerCommandBase(
        loritta: LorittaBot,
        labels: List<String>,
        val imageCount: Int,
        val descriptionKey: String,
        val endpoint: String,
        val fileName: String,
) : LorittaAbstractCommandBase(
        loritta,
        labels,
        CommandCategory.IMAGES
) {
    override fun command() = create {
        localizedDescription(descriptionKey)

        executes {
            val imagesData = (0 until imageCount).map {
                imageData(it) ?: run {
                    if (args.isEmpty())
                        explainAndExit()
                    else
                        fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
                }
            }

            val response = loritta.http.post<HttpResponse>("https://gabriela-canary.loritta.website$endpoint") {
                body = buildJsonObject {
                    putJsonArray("images") {
                        for (data in imagesData)
                            add(data)
                    }
                }.toString()
            }

            sendFile(response.receive(), fileName)
        }
    }
}

/**
 * Gets a [JsonObject] with the image data (image URL, base64, etc) from the current context.
 *
 * This is multiplatform because Loritta generates a image with a text if there isn't any matching images if the user
 * inputted arguments into the command. This can be true multiplatform without this method after that method is migrated
 * to support multiplatform1
 *
 * @param argument the position of the image in the command
 * @return         the image data, can be null if none was matched
 */
expect suspend fun CommandContext.imageData(argument: Int): JsonObject?