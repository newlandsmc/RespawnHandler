package me.cookie

import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

fun closestNumberToDivisibleBy(number: Float, divisibleBy: Int): Float { // Great name, I know.
    val quotient: Float = number / divisibleBy

    val n1: Float = divisibleBy * quotient

    val n2: Float = if (number * divisibleBy > 0) divisibleBy * (quotient + 1) else divisibleBy * (quotient - 1)

    return if (abs(number - n1) < abs(number - n2)) n1 else n2
}

val playerDamageBoostMap = mutableMapOf<UUID, DamageBoost>()

var Player.damageBoost: DamageBoost
    get() = playerDamageBoostMap[uniqueId] ?: DamageBoost(0, System.currentTimeMillis())
    set(value) {
        playerDamageBoostMap[uniqueId] = value
    }