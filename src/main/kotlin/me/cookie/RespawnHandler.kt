package me.cookie

import me.cookie.commands.SoulBound
import me.cookie.commands.SoulsAdmin
import me.cookie.cookiecore.data.sql.H2Storage
import me.cookie.listeners.*
import me.cookie.traits.CorpseTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.TraitInfo
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class RespawnHandler: JavaPlugin() {
    // I know, I know
    companion object {
        lateinit var instance: RespawnHandler
    }

    lateinit var playerSouls: H2Storage

    val itemChanceMap = mutableMapOf<Material, Chance>()
    override fun onEnable() {
        instance = this

        playerSouls = H2Storage(this, "PlayerSouls").apply {
            connect()
            initTable("player_souls", listOf("UUID varchar(255)", "SOULS int"))
        }

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CorpseTrait::class.java)
            .withName("CorpseTrait"))

        val pluginManager = getPluginManager()

        pluginManager.registerEvents(PlayerDeath(this), this)
        pluginManager.registerEvents(PlayerJoin(), this)
        pluginManager.registerEvents(PlayerQuit(), this)
        pluginManager.registerEvents(CorpseHitBoxClick(this), this)
        pluginManager.registerEvents(EntityDamageEntity(), this)

        getCommand("soulsadmin")!!.setExecutor(SoulsAdmin())
        getCommand("soulbound")!!.setExecutor(SoulBound())

        saveDefaultConfig()
        loadChances()

        Souls(this).startTask()
        DataUpdateRunnable().runTaskTimer(this, 20L, 20L)
    }

    private fun loadChances() {
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

    override fun onDisable() {
        CitizensAPI.getNPCRegistry().forEach {
            if(it.hasTrait(CorpseTrait::class.java)) {
                val trait = it.getOrAddTrait(CorpseTrait::class.java)
                trait.destroyHitBoxes()
            }
        }
    }
}
