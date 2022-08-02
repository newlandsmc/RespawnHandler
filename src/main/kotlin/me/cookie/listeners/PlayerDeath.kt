package me.cookie.listeners

import me.cookie.CorpseEntity
import me.cookie.DamageBoost
import me.cookie.RespawnHandler
import me.cookie.cookiecore.compressSimilarItems
import me.cookie.damageBoost
import me.cookie.data.DAMAGE_BOOST_DURATION
import me.cookie.data.DAMAGE_BOOST_MAX_STACK
import me.cookie.data.DAMAGE_BOOST_PERCENTAGE
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

        // ================ Damage Boost ====================

        val damageBoost = player.damageBoost

        println(DAMAGE_BOOST_PERCENTAGE * DAMAGE_BOOST_MAX_STACK)

        if(damageBoost.started + DAMAGE_BOOST_DURATION * 60000 < System.currentTimeMillis()){
            player.damageBoost = DamageBoost(0, System.currentTimeMillis())
        }
        if (damageBoost.percent != DAMAGE_BOOST_PERCENTAGE * DAMAGE_BOOST_MAX_STACK){
            player.damageBoost = DamageBoost(
                damageBoost.percent + DAMAGE_BOOST_PERCENTAGE, System.currentTimeMillis()
            )
        }

        // ================== Corpses =======================

        event.drops.clear()
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

        // If death is to lava/void, kill player's items (non soulbound)
        if(event.player.lastDamageCause == null){
            return
        }
        if (event.player.lastDamageCause?.cause == DamageCause.VOID) {
            return
        }else if (event.player.lastDamageCause?.cause == DamageCause.LAVA) {
            items.filter { it.type.name.contains("NETHERITE") }.forEach { item ->
                player.world.dropItem(
                    player.location,
                    item
                )
            }
            return
        }
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