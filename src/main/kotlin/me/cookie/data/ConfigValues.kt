package me.cookie.data

import me.cookie.RespawnHandler
import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(RespawnHandler::class.java)
private val config = plugin.config

val CORPSE_NAME: String = config.getString("corpse-name") ?: "(corpseName)'s corpse"
val CORPSE_LIMIT: Int = config.getInt("corpse-limit")
val CORPSE_DECAY_TIME: Long = config.getLong("corpse-decay-time")
val CORPSE_GRACE_PERIOD: Long = config.getLong("corpse-grace-period")
val CORPSE_LOCKED_MESSAGE: String = config.getString("corpse-grace-message")
    ?: "(corpseName)'s corpse is currently locked! It will be unlocked in (graceTimeLeft|dd:HH:mm:ss)"
val CORPSE_PICKEDUP_SOULS: String = config.getString("corpse-pickupsouls-message")
    ?: "<green>Found (corpseSouls) souls on (corpseName)'s corpse."
val CORPSE_PICKEDUP_NOSOULS: String = config.getString("corpse-nosouls-message")
    ?: "<red>No souls were found on this corpse."
val SOULBOUND_COST: Int = config.getInt("soulbound-cost")
val ROUND = config.getBoolean("round-up")