package me.cookie.listeners

import me.cookie.DamageDebuff
import me.cookie.cookiecore.formatMinimessage
import me.cookie.damageDebuff
import me.cookie.data.DAMAGE_DEBUFF_DURATION
import me.cookie.data.DAMAGE_DEBUFF_MAX_STACK
import me.cookie.data.DAMAGE_DEBUFF_MESSAGE
import me.cookie.data.DAMAGE_DEBUFF_PERCENTAGE
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageEntity: Listener{
    @EventHandler fun onEntityDamageEntity(event: EntityDamageByEntityEvent){
        if(event.damager !is Player) return
        if(event.entity !is Player) return
        val player = event.damager as Player
        val target = event.entity as Player


        /* ========== DAMAGE REDUCING ========== */
        val damageDebuff = player.damageDebuff
        if(damageDebuff.shouldEnd < System.currentTimeMillis()) {
            player.damageDebuff = DamageDebuff(0, System.currentTimeMillis())
        }else if(damageDebuff.percent > 0){
            event.damage *= (100f - damageDebuff.percent.toFloat()) * 0.01f
            println("Damage reduced by ${damageDebuff.percent}%")
        }

        /* ========== DEBUFF GIVER ========== */
        if (event.damage >= target.health) {
            println("Damage is greater than target health")

            if(damageDebuff.percent != DAMAGE_DEBUFF_PERCENTAGE * DAMAGE_DEBUFF_MAX_STACK) {
                println("Damage Debuff less than max stack")
                player.damageDebuff = DamageDebuff(
                    damageDebuff.percent + DAMAGE_DEBUFF_PERCENTAGE,
                    System.currentTimeMillis() + /*(5 * 1000)*/ (DAMAGE_DEBUFF_DURATION * 60000)
                )
                DAMAGE_DEBUFF_MESSAGE?.let { player.sendMessage(it.formatMinimessage()) }
            }
        }
    }
}
