package me.cookie.listeners

import net.citizensnpcs.api.event.NPCLeftClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class NPCHit: Listener {
    @EventHandler fun onNPCHit(event: NPCLeftClickEvent){
        event.npc.destroy()
    }
}