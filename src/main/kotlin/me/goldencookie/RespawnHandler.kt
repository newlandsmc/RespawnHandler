package me.goldencookie

import com.griefcraft.lwc.LWC
import com.griefcraft.lwc.LWCPlugin
import me.goldencookie.commands.SpawnLoot
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class RespawnHandler: JavaPlugin() {
    private var lwc: LWC? = null
    override fun onEnable() {
        val lwcPlugin = Bukkit.getPluginManager().getPlugin("LWC")

        if(lwcPlugin == null){
            logger.log(Level.SEVERE, "LWC not found, disabling...")
            Bukkit.getPluginManager().disablePlugin(this)
        }

        lwc = (lwcPlugin as LWCPlugin).lwc

        Bukkit.getPluginCommand("spawnloot")!!.setExecutor(SpawnLoot(this, lwc!!))
    }
}