package me.cookie.listeners

import me.cookie.loadSouls
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.trait.SkinTrait
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class PlayerJoin : Listener {
    @EventHandler fun onPlayerJoin(event: PlayerSpawnLocationEvent){
        event.player.loadSouls()

        // prevents issue where the skin is not loaded and the corpses are standing.
        // This basically allows for a smoother corpse experience.
        val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "").apply {
            getOrAddTrait(SkinTrait::class.java).skinName = event.player.name
        }
        val location = event.spawnLocation.clone().apply {
            y = 1000.0
        }
        npc.spawn(location)

        object: BukkitRunnable() {
            override fun run() {
                npc.destroy()
            }
        }.runTaskLater(CitizensAPI.getPlugin(), 40)
    }
}

