package me.cookie.listeners

import me.cookie.DamageBoost
import me.cookie.damageBoost
import me.cookie.data.DAMAGE_BOOST_DURATION
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageEntity: Listener{
    @EventHandler fun onEntityDamageEntity(event: EntityDamageByEntityEvent){
        if(event.damager !is Player) return
        val player = event.damager as Player
        val damageBoost = player.damageBoost
        if(damageBoost.percent > 0){
            if(damageBoost.started + (DAMAGE_BOOST_DURATION * 60000) < System.currentTimeMillis()) {
                player.damageBoost = DamageBoost(0, System.currentTimeMillis())
                return
            }
            event.damage *= 1 + (event.damage * (damageBoost.percent.toFloat() * 0.01))
        }

    }
}