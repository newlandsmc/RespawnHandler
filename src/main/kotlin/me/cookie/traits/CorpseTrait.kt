package me.cookie.traits

import me.cookie.cachedCorpses
import me.cookie.cookiecore.*
import me.cookie.menu.CorpseInventory
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.persistence.Persist
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.trait.HologramTrait
import net.citizensnpcs.util.PlayerAnimation
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.regex.Pattern

class CorpseTrait: Trait("CorpseTrait") {
    @Persist("CorpseOwnerName") var ownerName: String = ""
    @Persist("CorpseOwnerUUID") var ownerUUID: UUID = UUID.randomUUID()
    @Persist("CorpseItems") var itemstacks = ""
    @Persist("CorpseTimeSpawn") var timeSpawned = System.currentTimeMillis()
    @Persist("CorpseSpawnedBefore") private var spawnedBefore = false
    @Persist("Souls") var souls = 0

    private var isOpened = false
    var maxCorpses = 10
    var nameFormat: String = "(corpseName)'s corpse"
    var gracePeriod: Long = 600000
    var corpseLockedMessage: String =
        "(corpseName)'s corpse is currently locked! It will be unlocked in (graceTimeLeft|dd:HH:mm:ss)"

    lateinit var nameplate: HologramTrait

    var decayTime: Long = 172800000

    private val timePatternRegex: Pattern = Pattern.compile("\\((graceTimeLeft)\\|(.+)\\)")
    private var deserializedItemstacks = listOf<ItemStack>()

    override fun onSpawn() {
        if(spawnedBefore){
            // Needs a bit more of a delay for the skin loading. Skins are uncached on server restart
            object: BukkitRunnable() {
                override fun run() {
                    spawnSleeping()
                }
            }.runTaskLater(CitizensAPI.getPlugin(), 40)
        }else{
            spawnedBefore = true
            spawnSleeping()
        }
        deserializedItemstacks = itemstacks.deseralizeItemStacks()
        spawnedBefore = true

        // Destroy if there are more than limited corpses, also functions as a spawn limit :yay:
        val corpses = ownerUUID.cachedCorpses.toMutableList()

        if(corpses.size >= maxCorpses){
            corpses.first().destroy()
            corpses.removeFirst()
        }

        ownerUUID.cachedCorpses = corpses.apply {
            add(npc)
        }

        nameplate = npc.getOrAddTrait(HologramTrait::class.java)
    }

    fun spawnSleeping(){
        // Spawn a fake bed due to minecraft needing a bed to run the sleep animation, shortly removed afterwards
        val bedLoc = npc.storedLocation.clone().apply {
            // Blocks can't be placed at y 1000 anyway so no need to worry
            y = 1000.0
        }
        bedLoc.block.type = Material.BLACK_BED

        object: BukkitRunnable() {
            override fun run() {
                (npc.entity as Player).sleep(bedLoc.clone(), true)
                PlayerAnimation.SLEEP.play(npc.entity as Player)
                bedLoc.block.type = Material.AIR
                npcTimers()
            }
        }.runTaskLater(CitizensAPI.getPlugin(), 3)
    }

    @EventHandler fun onClick(event: NPCRightClickEvent){
        val clicker = event.clicker ?: return
        if(isOpened) { // no duping (hee hee hee haw)
            clicker.sendMessage("<red>This corpse is currently opened!".formatMinimessage())
            return
        }
        isOpened = true
        if(timeSpawned + gracePeriod >= System.currentTimeMillis() && clicker.uniqueId != ownerUUID) {
            val matcher = timePatternRegex.matcher(corpseLockedMessage)
            var message = corpseLockedMessage
            if(matcher.find()){
                if(message.contains("graceTimeLeft")){
                    val matchedGroup2 = matcher.group(2)
                    if(matchedGroup2 != null){
                        message = message
                            .replace(
                                Regex(timePatternRegex.pattern()),
                                ((timeSpawned + gracePeriod) - System.currentTimeMillis())
                                    .formatMillis(matchedGroup2)
                            )
                    }
                }
            }
            message = message
                .replace("(corpseName)", ownerName)
                .formatPlayerPlaceholders(clicker)
            clicker.sendMessage(message.formatMinimessage())
            return
        }

        clicker.openMenu(CorpseInventory(clicker.playerMenuUtility, event.npc, deserializedItemstacks, nameFormat))
    }

    fun npcTimers(){
        if(npc == null) return

        val graceEndsWhen = timeSpawned + gracePeriod
        val decayWhen = timeSpawned + decayTime + gracePeriod

        var time = graceEndsWhen
        var prefix = "Unlocks in:"
        var decaying = false

        object: BukkitRunnable() {
            override fun run() {
                if(npc == null) cancel()

                if(time - System.currentTimeMillis() <= 0){
                    if(!decaying){
                        time = decayWhen
                        prefix = "Decays in:"
                        decaying = true
                    }
                    if(decayWhen - System.currentTimeMillis() <= 0){
                        ownerUUID.cachedCorpses = ownerUUID.cachedCorpses
                            .toMutableList()
                            .apply {
                                remove(npc)
                            }
                        npc.destroy()
                        cancel()
                    }
                    return
                }
                // Citizens weirdos use a combination of vanilla color codes (ew) and hex
                nameplate.lineHeight = 1.0
                nameplate.setLine(
                    0,
                    "&#FF2D00$prefix ${(time - System.currentTimeMillis()).formatMillis("HH:mm:ss")}"
                )
            }
        }.runTaskTimer(CitizensAPI.getPlugin(), 20, 20)
    }
}
