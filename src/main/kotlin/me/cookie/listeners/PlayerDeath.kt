package me.cookie.listeners

import me.cookie.CorpseEntity
import me.cookie.RespawnHandler
import me.cookie.cookiecore.compressSimilarItems
import me.cookie.data.ROUND
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
        event.drops.clear()
        plugin.logger.warning("Captured death for ${player.name}, at ${player.location.x}x ${player.location.y}y ${player.location.z}z")
        // Check if the player's inventory is empty.

        var items = player.inventory.contents.clone().toList().filterNotNull().filter { it.type != Material.AIR }

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
        if(items.isEmpty()) {
            plugin.logger.warning("no items to put in corpse, not spawning corpse")
            return
        }
        // If death is to lava/void, kill player's items (non soulbound)
        if(event.player.lastDamageCause == null){
            plugin.logger.warning("last damage cause was null, returning")
            return
        }
        if (event.player.lastDamageCause?.cause == DamageCause.VOID) {
            plugin.logger.warning("Damage cause is void, not spawning corpse.")
            return
        }else if (event.player.lastDamageCause?.cause == DamageCause.LAVA) {
            plugin.logger.warning("Dropping any netherite items")
            items.filter { it.type.name.contains("NETHERITE") }.forEach { item ->
                player.world.dropItem(
                    player.location,
                    item
                )
            }
            return
        }

        plugin.logger.warning("successfully spawned corpse for ${player.name}")
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