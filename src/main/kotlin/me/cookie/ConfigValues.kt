package me.cookie

import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(RespawnHandler::class.java)
val CORPSE_NAME: String = plugin.config.getString("corpse-name") ?: "(corpseName)'s corpse"
val CORPSE_LIMIT: Int = plugin.config.getInt("corpse-limit")
val CORPSE_DECAY_TIME: Long = plugin.config.getLong("corpse-decay-time")
val CORPSE_GRACE_PERIOD: Long = plugin.config.getLong("corpse-grace-period")
val CORPSE_LOCKED_MESSAGE: String = plugin.config.getString("corpse-grace-message")
    ?: "(corpseName)'s corpse is currently locked! It will be unlocked in (graceTimeLeft|dd:HH:mm:ss)"
val SOULBOUND_COST: Int = plugin.config.getInt("soulbound-cost")