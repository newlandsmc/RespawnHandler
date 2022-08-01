package me.cookie.data

import net.citizensnpcs.api.npc.NPC
import org.bukkit.entity.Player
import java.util.*

private val cachedCorpsesMap = hashMapOf<UUID, List<NPC>>()

var Player.cachedCorpses: List<NPC>
    get() {
        return this.uniqueId.cachedCorpses.toMutableList()
    }
    set(value) {
        this.uniqueId.cachedCorpses = value
    }

var UUID.cachedCorpses: List<NPC>
    get() {
        return cachedCorpsesMap[this] ?: emptyList()
    }
    set(value) {
        cachedCorpsesMap[this] = value
    }
