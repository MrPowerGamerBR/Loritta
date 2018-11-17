package com.mrpowergamerbr.loritta.utils.debug

import com.mongodb.Mongo
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.amino.AminoRepostTask
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.NewRssFeedTask
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import java.io.File
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object DebugLog {
	var cancelAllEvents = false

	fun startCommandListenerThread() {
		thread {
			commandLoop@ while (true) {
				try {
					val line = readLine()!!
					handleLine(line)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	fun handleLine(line: String) {
		val args = line.split(" ").toMutableList()
		val command = args[0]
		args.removeAt(0)

		when (command) {
			"toggleevents" -> {
				cancelAllEvents = !cancelAllEvents

				println("Cancel all events: ${cancelAllEvents}")
			}
			"reload" -> {
				val arg0 = args.getOrNull(0)

				if (arg0 == "commands") {
					LorittaLauncher.loritta.loadCommandManager()
					println("${com.mrpowergamerbr.loritta.utils.loritta.commandManager.commandMap.size} comandos carregados")
					return
				}
				if (arg0 == "mongo") {
					LorittaLauncher.loritta.initMongo()
					println("MongoDB recarregado!")
					return
				}
			}
			"info" -> {
				val mb = 1024 * 1024
				val runtime = Runtime.getRuntime()
				println("===[ INFO ]===")
				println("Shards: ${lorittaShards.getShards().size}")
				println("Total Servers: ${lorittaShards.getGuildCount()}")
				println("Used Memory:"
						+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
				println("Free Memory:"
						+ runtime.freeMemory() / mb);
				println("Total Memory:" + runtime.totalMemory() / mb);
				println("Max Memory:" + runtime.maxMemory() / mb);
			}
			"shards" -> {
				val shards = lorittaShards.getShards()

				for ((index, shard) in shards.withIndex()) {
					println("SHARD $index (${shard.status.name} - ${shard.ping}ms): ${shard.guilds.size} guilds - ${shard.users.size} members")
				}
			}
			"extendedinfo" -> {
				println("===[ EXTENDED INFO ]===")
				println("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
				println("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
				println("dummyServerConfig.guildUserData.size: ${loritta.dummyServerConfig.guildUserData.size}")
				println("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
				println("locales.size: ${loritta.locales.size}")
				println("ignoreIds.size: ${loritta.ignoreIds.size}")
				println("userCooldown.size: ${loritta.userCooldown.size}")
				println("musicManagers.size: ${loritta.audioManager.musicManagers.size}")
				println("songThrottle.size: ${loritta.audioManager.songThrottle.size}")
				println("youTubeKeys.size: ${loritta.youtubeKeys.size}")
				println("fanArts.size: ${loritta.fanArts.size}")
				println("storedLastEntries.size: ${AminoRepostTask.storedLastIds.size}")
				println("gameInfoCache.size: ${NewLivestreamThread.gameInfoCache.size}")
				println("isLivestreaming.size: ${NewLivestreamThread.isLivestreaming.size}")
				println("displayNameCache.size: ${NewLivestreamThread.displayNameCache.size}")
				println("storedLastEntries.size: ${NewRssFeedTask.storedLastEntries.size}")
				println("cachedInviteLinks.size: ${InviteLinkModule.cachedInviteLinks.size}")
				println("detectedInviteLinks.size: ${InviteLinkModule.detectedInviteLinks.size}")
			}
			"threads" -> {
				println("===[ ACTIVE THREADS ]===")
				println("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
				println("oldCorotuineExecutor: ${(loritta.oldCoroutineExecutor as ThreadPoolExecutor).activeCount}")
				println("coroutineExecutor: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}")
				println("Total Thread Count: ${Thread.getAllStackTraces().keys.size}")
			}
			"mongo" -> {
				println("===[ MONGODB ]===")
				println("isLocked: " + loritta.mongo.isLocked)

				val clusterField = Mongo::class.java.getDeclaredField("cluster")
				clusterField.isAccessible = true
				val cluster = clusterField.get(loritta.mongo)
				println(cluster)
				val serverField = cluster::class.java.getDeclaredField("server")
				serverField.isAccessible = true
				val defServer = serverField.get(cluster)
				println(defServer)
				val conPoolField = defServer::class.java.getDeclaredField("connectionPool")
				conPoolField.isAccessible = true
				val conPool = conPoolField.get(defServer)
				println(conPool)
				val waitQueueField = conPool::class.java.getDeclaredField("waitQueueSize")
				waitQueueField.isAccessible = true
				val waitQueueSize = waitQueueField.get(conPool) as AtomicInteger
				println("Wait Queue Size: " + waitQueueSize.get())
			}
			"databases" -> {
				val findProfilePostgreAvg = loritta.findProfilePostgre.toTypedArray().mapNotNull { it }.average()
				val findProfileMongoAvg = loritta.findProfileMongo.toTypedArray().mapNotNull { it }.average()
				val newProfilePostgreAvg = loritta.newProfilePostgre.toTypedArray().mapNotNull { it }.average()

				println("findProfilePostgre (${loritta.idx0}): $findProfilePostgreAvg nanosegundos (${findProfilePostgreAvg / 1000000})")
				println("findProfileMongo (${loritta.idx1}): $findProfileMongoAvg nanosegundos (${findProfileMongoAvg / 1000000})")
				println("newProfilePostgre (${loritta.idx2}): $newProfilePostgreAvg nanosegundos (${newProfilePostgreAvg / 1000000})")

				var text = "===[ findProfilePostgre ($findProfilePostgreAvg nanosegundos) ]===\n"
				loritta.findProfilePostgre.toTypedArray().mapNotNull { it }.forEach {
					text += "$it nanosegundos\n"
				}
				text += "\n\n===[ findProfileMongo ($findProfileMongoAvg nanosegundos) ]===\n"
				loritta.findProfileMongo.toTypedArray().mapNotNull { it }.forEach {
					text += "$it nanosegundos\n"
				}
				text += "\n\n===[ newProfilePostgre ($newProfilePostgreAvg nanosegundos) ]===\n"
				loritta.newProfilePostgre.toTypedArray().mapNotNull { it }.forEach {
					text += "$it nanosegundos\n"
				}

				File("database-results.txt").writeText(text)
			}
			"bomdiaecia" -> {
				loritta.bomDiaECia.handleBomDiaECia(true)
			}
		}
	}
}