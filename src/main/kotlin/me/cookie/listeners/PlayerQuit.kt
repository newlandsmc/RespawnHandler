package me.cookie.listeners

import me.cookie.saveSouls
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuit: Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.player.saveSouls()
    }
}