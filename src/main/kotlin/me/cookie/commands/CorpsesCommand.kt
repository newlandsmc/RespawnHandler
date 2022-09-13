package me.cookie.commands

import me.cookie.CorpseEntity
import me.cookie.RespawnHandler
import me.cookie.cookiecore.deseralizeItemStacks
import me.cookie.cookiecore.formatMinimessage
import me.cookie.data.Corpse
import me.cookie.data.buildCorpseFromResult
import me.cookie.data.getCorpsesConnection
import me.cookie.util.TimeAgo
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import java.text.SimpleDateFormat
import java.util.*

class CorpsesCommand(private val plugin: RespawnHandler) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // /corpses history <player> <page>
        // /corpses spawn <player> <id>
        if (args.size < 1) {
            sendUsage(sender)
            return true
        }
        val subCommand = args[0].lowercase()
        when (subCommand) {
            "history" -> {
                val player = args[1]
                var page: Int;
                if (args.size < 3) {
                    page = 1
                } else {
                    page = args[2].toInt()
                }
                sender.sendMessage("<green>Running database query...".formatMinimessage())
                runAsync {
                    val connection = getCorpsesConnection()
                    val pageStart = (page - 1) * CORPSES_PER_PAGE
                    val offlinePlayer = plugin.server.getOfflinePlayer(player)
                    val uuid = offlinePlayer.uniqueId
                    val preparedStatement =
                        connection.prepareStatement("SELECT * FROM corpses WHERE uuid = '$uuid' ORDER BY id DESC LIMIT $pageStart, $CORPSES_PER_PAGE")
                    val results = preparedStatement.executeQuery()
                    val corpses = mutableListOf<Corpse>()
                    while (results.next()) {
                        corpses.add(buildCorpseFromResult(results))
                    }
                    sender.sendMessage("--------- <blue>Lookup Results</blue> ---------".formatMinimessage())
                    for (corpse in corpses) {
                        //sender.sendMessage("<green>${corpse.id}: <yellow>${corpse.x}, ${corpse.y}, ${corpse.z}</yellow> ${corpse.world} ${corpse.cause} ${corpse.timestamp} ${corpse.claimed}".formatMinimessage())
                        val timestamp: Long = corpse.timestamp
                        //val dateFormat = SimpleDateFormat("MM/dd hh:mm:ss")
                        //val date = dateFormat.format(timestamp)
                        val date = TimeAgo.toDurationUnixMillis(timestamp)
                        var status: String;
                        if (corpse.claimed) {
                            val claimedByWho =
                                plugin.server.getOfflinePlayer(UUID.fromString(corpse.claimedByUUID)).name
                            status = "<green>Claimed By ${claimedByWho}</green>"
                        } else if (corpse.expired) {
                            status = "<red>Expired</red>"
                        } else {
                            status = "<yellow>Unclaimed</yellow>"
                        }
                        sender.sendMessage("<grey>${date} <red>- <white>${corpse.cause} <grey>(#${corpse.id})".formatMinimessage())
                        sender.sendMessage("<grey>(x${corpse.x}/y${corpse.y}/z${corpse.z}/${corpse.world}) Items: ${corpse.items} Status: ${status}".formatMinimessage())
                    }
                    sender.sendMessage("--------- <blue>Lookup Results</blue> ---------".formatMinimessage())
                    val s = "<grey>${corpses.size} results found. for page #${page}"
                    sender.sendMessage(s.formatMinimessage())
                }
            }

            "spawn" -> {
                val player = args[1]
                val id = args[2].toInt()
                sender.sendMessage("<green>Running database query...".formatMinimessage())
                runAsync {
                    val connection = getCorpsesConnection()
                    val offlinePlayer = plugin.server.getOfflinePlayer(player)
                    val preparedStatement =
                        connection.prepareStatement("SELECT * FROM corpses WHERE uuid = '${offlinePlayer.uniqueId}' AND id = $id")
                    val results = preparedStatement.executeQuery()
                    if (!results.next()) {
                        sender.sendMessage("<red>No corpse found with id $id".formatMinimessage())
                        return@runAsync
                    }
                    val corpse = buildCorpseFromResult(results)
                    val location = Location(
                        plugin.server.getWorld(corpse.world),
                        corpse.x.toDouble(),
                        corpse.y.toDouble(),
                        corpse.z.toDouble()
                    )
                    val items = results.getString("inventory").deseralizeItemStacks()
                    val corpseName: String = offlinePlayer.name.toString();
                    runSync {
                        val corpseE = CorpseEntity(corpseName, offlinePlayer.uniqueId, 0, items, corpse)
                        corpseE.spawnCorpse(location)
                        sender.sendMessage("<green>Spawned the corpse at ${corpse.x}, ${corpse.y}, ${corpse.z}".formatMinimessage())
                    }
                }
            }

            else -> {
                sender.sendMessage("<red>Unknown sub-command: $subCommand".formatMinimessage())
                sendUsage(sender)
            }
        }
        return true
    }

    fun sendUsage(sender: CommandSender) {
        sender.sendMessage("<red>Usage:".formatMinimessage())
        sender.sendMessage("<red>/corpses history <player> <page>".formatMinimessage())
        sender.sendMessage("<red>/corpses spawn <player> <id>".formatMinimessage())
    }

    fun runAsync(runnable: () -> Unit) {
        plugin.server.scheduler.runTaskAsynchronously(plugin, runnable)
    }

    fun runSync(runnable: () -> Unit) {
        plugin.server.scheduler.runTask(plugin, runnable)
    }

    companion object {
        const val CORPSES_PER_PAGE = 5

    }
}
