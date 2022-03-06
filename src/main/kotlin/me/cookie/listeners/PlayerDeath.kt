package me.cookie.listeners

import me.cookie.CorpseEntity
import me.cookie.RespawnHandler
import me.cookie.cookiecore.compressSimilarItems
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.properties.Delegates

class PlayerDeath(private val plugin: RespawnHandler): Listener {
    private var maxCorpses by Delegates.notNull<Int>()

    init {
        maxCorpses = plugin.config.getInt("corpse-limit")
    }

    @EventHandler fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        // If death is to lava/void, kill player's items
        if (event.player.lastDamageCause?.cause == DamageCause.LAVA
            || event.player.lastDamageCause?.cause == DamageCause.VOID) return
        // Check if the player's inventory is empty.
        if (player.inventory.contents == null) return
        if (player.inventory.contents!!.isEmpty()) return
        val clonedItems = player.inventory.contents ?: arrayOf()

        var items = clonedItems.clone().toList().filterNotNull().filter { it.type != Material.AIR }
        if(items.isEmpty()) return
        items = items.compressSimilarItems()

        items.forEach { item ->
            val chance = plugin.itemChanceMap[item.type]
            if (chance != null) {
                item.amount = round((chance.next().toDouble() / 100.0) * item.amount.toDouble())
            }
        }

        event.drops.clear()
        val corpse = CorpseEntity(player, items)
        corpse.spawnCorpse(player.location)
    }

    private fun round(double: Double): Int {
        return if(round) {
            ceil(double).toInt()
        } else {
            floor(double).toInt()
        }
    }

    private val round = plugin.config.getBoolean("round-up")
}