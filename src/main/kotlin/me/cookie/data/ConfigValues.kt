package me.cookie.data

import me.cookie.RespawnHandler
import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(RespawnHandler::class.java)
private val config = plugin.config

val CORPSE_NAME: String = config.getString("corpse-name", "(corpseName)'s corpse")!!

val CORPSE_LIMIT: Int = config.getInt("corpse-limit", 10)

val CORPSE_DECAY_TIME: Long = config.getLong("corpse-decay-time", 2880)

val CORPSE_GRACE_PERIOD: Long = config.getLong("corpse-grace-period", 10)

val CORPSE_LOCKED_MESSAGE: String = config.getString("corpse-grace-message",
    "(corpseName)'s corpse is currently locked! It will be unlocked in (graceTimeLeft|dd:HH:mm:ss)")!!

val CORPSE_PICKEDUP_SOULS: String = config.getString("corpse-pickupsouls-message",
    "<green>Found (corpseSouls) souls on (corpseName)'s corpse.")!!

val CORPSE_PICKEDUP_NOSOULS: String = config.getString("corpse-nosouls-message",
    "<red>No souls were found on this corpse.")!!

val SOULBOUND_COST: Int = config.getInt("soulbound-cost", 50)

val ROUND = config.getBoolean("round-up", false)

val DAMAGE_DEBUFF_DURATION = config.getInt("damage-debuff-duration", 5)

val DAMAGE_DEBUFF_PERCENTAGE = config.getInt("damage-debuff-percentage", 10)

val DAMAGE_DEBUFF_MAX_STACK = config.getInt("damage-debuff-max-stack", 10)
