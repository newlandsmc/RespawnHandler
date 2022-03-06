package me.cookie

import me.cookie.listeners.NPCHit
import me.cookie.listeners.PlayerDeath
import me.cookie.listeners.PlayerJoin
import me.cookie.traits.CorpseTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.TraitInfo
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class RespawnHandler: JavaPlugin() {
    val itemChanceMap = mutableMapOf<Material, Chance>()
    override fun onEnable() {

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CorpseTrait::class.java)
            .withName("CorpseTrait"))

        getPluginManager().registerEvents(PlayerDeath(this), this)
        getPluginManager().registerEvents(PlayerJoin(), this)
        getPluginManager().registerEvents(NPCHit(), this)

        saveDefaultConfig()
        loadChances()
    }

    private fun loadChances(){
        val items = config.getConfigurationSection("items")!!.getKeys(false)
        items.forEach {
            val item = Material.valueOf(it)
            val min = config.getInt("items.$it.min")
            val max = config.getInt("items.$it.max")

            // Check if item is already in the map
            if(itemChanceMap.containsKey(item)) {
                logger.log(Level.WARNING, "Item $item is already in the map, skipping...\n" +
                        "Please check your config.yml for duplicate items.")
                return@forEach
            }

            itemChanceMap[item] = Chance(min, max)
        }
    }
}