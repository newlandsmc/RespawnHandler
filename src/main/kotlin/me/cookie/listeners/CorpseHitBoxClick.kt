package me.cookie.listeners

import me.cookie.RespawnHandler
import me.cookie.traits.CorpseTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

class CorpseHitBoxClick(private val plugin: RespawnHandler): Listener {
    @EventHandler fun onCorpseHitBoxClick(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        //if(entity.type != EntityType.BEE) return

        if(!entity.persistentDataContainer.has(NamespacedKey(plugin, "corpse_Id"))) return

        event.isCancelled = true

        val id = entity.persistentDataContainer
            .get(NamespacedKey(plugin, "corpse_Id"), PersistentDataType.INTEGER)!!
        val npc = CitizensAPI.getNPCRegistry().getById(id)
        npc?.getOrAddTrait(CorpseTrait::class.java)?.onClick(NPCRightClickEvent(npc, event.player))
    }
}