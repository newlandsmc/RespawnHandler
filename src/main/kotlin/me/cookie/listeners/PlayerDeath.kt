package me.cookie.listeners

import me.cookie.CorpseEntity
import me.cookie.RespawnHandler
import me.cookie.cookiecore.compressSimilarItems
import me.cookie.cookiecore.serialize
import me.cookie.data.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.properties.Delegates

class PlayerDeath(private val plugin: RespawnHandler) : Listener {
    private var maxCorpses by Delegates.notNull<Int>()

    init {
        maxCorpses = plugin.config.getInt("corpse-limit")
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player

        // ================== Corpses =======================

        event.drops.clear()
        var items = player.inventory.contents.clone().toList().filterNotNull().filter { it.type != Material.AIR }

        items = items.compressSimilarItems()

        val soulboundedItems = mutableListOf<ItemStack>()
        var message = event.deathMessage
        if (message == null || message.isEmpty()) {
            val cause = event.entity.getLastDamageCause()
            if (cause != null) message = "Cause: " + cause
            else message = "Cause: Unknown"
        }

        items.forEach { item ->
            if (item.itemMeta != null) {
                val soulbounded = item.itemMeta.persistentDataContainer
                    .has(NamespacedKey(plugin, "soulbounded"), PersistentDataType.BYTE)
                if (soulbounded) {
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
        val base64Data = items.serialize()

        // If death is to lava/void, kill player's items (non soulbound)
        if (event.player.lastDamageCause == null) {
            return
        }
        if (event.player.lastDamageCause?.cause == DamageCause.VOID) {
            return
        } else if (event.player.lastDamageCause?.cause == DamageCause.LAVA) {
            items.filter { it.type.name.contains("NETHERITE") }.forEach { item ->
                player.world.dropItem(
                    player.location,
                    item
                )
            }
            return
        }
        val location = player.location

        val corpseData = Corpse(
            getNextCorpseId(player.uniqueId), player.uniqueId.toString(), location.x.toInt(), location.y.toInt(), location.z.toInt(),
            location.world.name, message, base64Data, System.currentTimeMillis(), false, items.size,false, ""
        )
        saveCorpse(corpseData)
        val corpse = CorpseEntity(player.name, player.uniqueId, player.souls, items, corpseData) //CorpseEntity(player, items, corpseData)
        corpse.spawnCorpse(player.location)
    }

    private fun round(double: Double): Int {
        return if (ROUND) {
            ceil(double).toInt()
        } else {
            floor(double).toInt()
        }
    }
}
