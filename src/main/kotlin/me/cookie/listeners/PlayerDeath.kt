package me.cookie.listeners

import me.cookie.CorpseEntity
import me.cookie.ROUND
import me.cookie.RespawnHandler
import me.cookie.cookiecore.compressSimilarItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
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
        event.drops.clear()
        // Check if the player's inventory is empty.
        if (player.inventory.contents == null) return

        var items = player.inventory.contents!!.clone().toList().filterNotNull().filter { it.type != Material.AIR }

        items = items.compressSimilarItems()

        val soulboundedItems = mutableListOf<ItemStack>()

        items.forEach { item ->
            if(item.itemMeta != null) {
                val soulbounded = item.itemMeta.persistentDataContainer
                    .has(NamespacedKey(plugin, "soulbounded"), PersistentDataType.BYTE)
                if(soulbounded) {
                    soulboundedItems.add(item)
                    event.itemsToKeep.add(item)
                    return@forEach
                }
                return@forEach
            }

            val chance = plugin.itemChanceMap[item.type]
            if (chance != null) {
                item.amount = item.amount - round((chance.next().toDouble() / 100.0) * item.amount.toDouble())
            }
        }

        items.removeAll(soulboundedItems)

        // Don't spawn corpse if there's no items to place in it
        if(items.isEmpty()) return

        val corpse = CorpseEntity(player, items)
        corpse.spawnCorpse(player.location)
    }

    private fun round(double: Double): Int {
        return if(ROUND) {
            ceil(double).toInt()
        } else {
            floor(double).toInt()
        }
    }
}