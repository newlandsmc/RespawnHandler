package me.cookie

import me.cookie.cookiecore.formatMinimessage
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class DataUpdateRunnable: BukkitRunnable() {
    override fun run() {
        val iter = playerDamageDebuffMap.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            //val k = entry.key
            val v = entry.value
            val player = Bukkit.getPlayer(entry.key)
            if (player != null) {
                if (v.shouldEnd < System.currentTimeMillis()) {
                    iter.remove()
                    player.sendMessage("<green>You no longer have blood on your hands.".formatMinimessage())
                }
            }
        }
    }
}
