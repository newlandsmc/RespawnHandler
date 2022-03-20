package me.cookie

import me.cookie.cookiecore.data.Values
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable

class Souls(private val plugin: RespawnHandler) {
    init {
        pluginInstance = plugin
    }
    fun startTask() {
        object: BukkitRunnable() {
            override fun run() {
                pluginInstance.server.onlinePlayers.forEach {
                    it.saveSouls()
                }
            }
        }.runTaskTimer(plugin, 18000, 18000)
    }
}

private lateinit var pluginInstance: RespawnHandler

var Player.souls: Int
    get() = this.getMetadata("souls")[0].asInt()
    set(value) {
        this.setMetadata("souls", FixedMetadataValue(pluginInstance, value))
    }

fun Player.loadSouls() {
    val souls = pluginInstance.playerSouls
        .getRowsWhere("player_souls", "SOULS", "UUID = '${this.uniqueId}'")
    if(souls.isEmpty()) {
        this.initIntoTable()
        this.souls = 0
    } else {
        if(souls[0].values.isEmpty()) {
            this.souls = 0
        } else {
            this.souls = (souls[0].values[0] as String).toInt()
        }
    }
}

fun Player.saveSouls() {
    pluginInstance.playerSouls
        .updateColumnsWhere(
            "player_souls",
            listOf("SOULS"),
            "UUID = '${this.uniqueId}'",
            Values("${this.souls}")
        )
}

fun Player.initIntoTable() {
    pluginInstance.playerSouls
        .insertIntoTable(
            "player_souls",
            listOf("UUID", "SOULS"),
            Values("${this.uniqueId}", "0")
        )
}