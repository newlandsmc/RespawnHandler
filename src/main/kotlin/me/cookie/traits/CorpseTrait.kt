package me.cookie.traits

import me.cookie.RespawnHandler
import me.cookie.data.cachedCorpses
import me.cookie.closestNumberToDivisibleBy
import me.cookie.cookiecore.deseralizeItemStacks
import me.cookie.cookiecore.formatMillis
import me.cookie.cookiecore.formatMinimessage
import me.cookie.cookiecore.formatPlayerPlaceholders
import me.cookie.data.setCorpseClaimed
import me.cookie.data.setCorpseExpired
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.persistence.Persist
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.trait.HologramTrait
import net.citizensnpcs.util.PlayerAnimation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Turtle
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.regex.Pattern

class CorpseTrait: Trait("CorpseTrait") {
    var plugin: RespawnHandler = RespawnHandler.instance

    @Persist("CorpseOwnerName") var ownerName: String = ""
    @Persist("CorpseOwnerUUID") var ownerUUID: UUID = UUID.randomUUID()
    @Persist("CorpseItems") var itemstacks = ""
    @Persist("CorpseTimeSpawn") var timeSpawned = System.currentTimeMillis()
    @Persist("CorpseSpawnedBefore") private var spawnedBefore = false
    @Persist("Souls") var souls = 0
    @Persist("CorpseDecayTime") var decayTime: Long = 172800000
    @Persist("CorpseGraceTime") var gracePeriod: Long = 600000
    @Persist("LastClicked") var lastClicked: Long = System.currentTimeMillis()
    @Persist("CorpseId") var id = -1

    private val hitBoxes = mutableListOf<Entity>()
    /*private var isOpened = false*/
    var maxCorpses = 10
    var nameFormat: String = "(corpseName)'s corpse"

    var corpseLockedMessage: String =
        "(corpseName)'s corpse is currently locked! It will be unlocked in (graceTimeLeft|dd:HH:mm:ss)"

    lateinit var nameplate: HologramTrait

    private val timePatternRegex: Pattern = Pattern.compile("\\((graceTimeLeft)\\|(.+)\\)")
    private var deserializedItemstacks = listOf<ItemStack>()

    override fun onSpawn() {
        plugin.logger.info("CorpseTrait onSpawn called!")
        /*
        if(spawnedBefore) {
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
         */
        if (npc == null) {
            plugin.logger.info("npc is null!")
            return
        }
        if (npc.entity == null) {
            plugin.logger.info("Entity is null!")
            return
        }

        object: BukkitRunnable() {
            override fun run() {
                spawnSleeping()
            }
        }.runTaskLater(CitizensAPI.getPlugin(), 40)
        deserializedItemstacks = itemstacks.deseralizeItemStacks()
        spawnedBefore = true

        // Destroy if there are more than limited corpses, also functions as a spawn limit :yay:
        val corpses = ownerUUID.cachedCorpses.toMutableList()

        if(corpses.size >= maxCorpses) {
            corpses.first().getOrAddTrait(CorpseTrait::class.java).destroyCorpse()
            corpses.removeFirst()
        }

        ownerUUID.cachedCorpses = corpses.apply {
            add(npc)
        }

        nameplate = npc.getOrAddTrait(HologramTrait::class.java)
    }

    fun spawnSleeping() {
        plugin.logger.info("Spawning sleeping player...")
        (npc.entity as Player).sleep(npc.storedLocation.clone(), true)
        PlayerAnimation.SLEEP.play(npc.entity as Player)

        npcTimers()
        spawnHitBoxes()
    }

    @EventHandler fun onClick(event: NPCRightClickEvent) {
        val clicker = event.clicker ?: return
        if(lastClicked + 1000 >= System.currentTimeMillis()) return
        lastClicked = System.currentTimeMillis()

        /*val trait = event.npc.getOrAddTrait(CorpseTrait::class.java)
        if(trait.isOpened) { // no duping (hee hee hee haw)
            return
        }*/
        if(isLocked() && clicker.uniqueId != ownerUUID) {
            val matcher = timePatternRegex.matcher(corpseLockedMessage)
            var message = corpseLockedMessage
            if(matcher.find()) {
                if(message.contains("graceTimeLeft")) {
                    val matchedGroup2 = matcher.group(2)
                    if(matchedGroup2 != null) {
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

        val size = deserializedItemstacks.size
        deserializedItemstacks.forEach {
            npc.storedLocation.world.dropItem(npc.storedLocation, it)
        }
        //clicker.sendMessage("<green>Dropped $size items from your corpse!".formatMinimessage())
        plugin.logger.info("Dropped $size items from ${ownerName}'s corpse, claimed by ${clicker.name}")
        ownerUUID.cachedCorpses = ownerUUID.cachedCorpses.filter { it != npc }
        destroyCorpse()
        Bukkit.getServer().scheduler.runTaskAsynchronously(RespawnHandler.instance, Runnable {
            setCorpseClaimed(this.ownerUUID, id, true, event.clicker.uniqueId)
        })

        /*clicker.openMenu(CorpseInventory(clicker.playerMenuUtility, event.npc, deserializedItemstacks, nameFormat))
        trait.isOpened = true*/
    }

    private fun isLocked(): Boolean {
        return timeSpawned + gracePeriod >= System.currentTimeMillis()
    }

    private fun spawnHitBoxes() {
        val location = npc.storedLocation.clone().apply {
            yaw = closestNumberToDivisibleBy(90f + -yaw, 45)
        }

        val clickLoc1 = location
        val clickLoc2 = location

        fun spawnHitBox(location: Location) { // We do a little brain damage
            hitBoxes.add(location.world.spawnEntity(
                location,
                EntityType.TURTLE
            ).apply {
                (this as Turtle)
                isInvulnerable = true
                isSilent = true
                setGravity(false)
                setAI(false)
                addPotionEffect(
                    PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false
                    )
                )
                persistentDataContainer.set(
                    NamespacedKey(plugin, "corpse_Id"),
                    PersistentDataType.INTEGER,
                    npc.id
                )
            })
        }

        spawnHitBox(clickLoc1)
        spawnHitBox(clickLoc2)

        syncHitBoxes()
    }

    fun syncHitBoxes() {
        val storedLoc = npc.storedLocation.clone().apply {
            yaw = closestNumberToDivisibleBy(90f + -yaw, 45)
            y -= 0.2
        }

        hitBoxes[0].teleport(storedLoc
            .apply {
                add(
                    direction.apply {
                        x *= 0.5
                        z *= 0.5
                    }
                )
            }
        )

        hitBoxes[1].teleport(storedLoc
            .apply {
                add(
                    direction.apply {
                        x *= 0.9
                        z *= 0.9
                    }
                )
            }
        )
    }

    private fun npcTimers() {
        if(npc == null) return

        val graceEndsWhen = timeSpawned + gracePeriod
        val decayWhen = timeSpawned + decayTime + gracePeriod

        var time = graceEndsWhen
        var prefix = "&#fff000Public in:"
        var decaying = false

        object: BukkitRunnable() {
            var lastPos = npc.storedLocation.clone()
            override fun run() {
                if(npc == null || npc.entity == null) {
                    cancel()
                    return
                }

                // HitBox Sync
                if(lastPos != npc.storedLocation) {
                    syncHitBoxes()
                    lastPos = npc.storedLocation.clone()
                }

                // Corpse Decaying
                if(time - System.currentTimeMillis() <= 0) {
                    if(!decaying) {
                        time = decayWhen
                        prefix = "&#d5d5d5Decays in:"
                        decaying = true
                    }
                    if(decayWhen - System.currentTimeMillis() <= 0) {
                        ownerUUID.cachedCorpses = ownerUUID.cachedCorpses.filter { it != npc }
                        npc.getOrAddTrait(CorpseTrait::class.java).destroyCorpse()
                        setCorpseExpired(this@CorpseTrait.ownerUUID, id)
                        cancel()
                    }
                    return
                }
                // Citizens weirdos use a combination of vanilla color codes (ew) and hex
                nameplate.lineHeight = 1.0
                nameplate.setLine(
                    0,
                    "$prefix ${(time - System.currentTimeMillis()).formatMillis("HH:mm:ss")}"
                )
            }
        }.runTaskTimer(CitizensAPI.getPlugin(), 20, 20)
    }

    fun destroyHitBoxes() {
        hitBoxes.forEach {
            it.remove()
        }
    }

    override fun onDespawn() {
        destroyHitBoxes()
    }

    fun destroyCorpse() {
        destroyHitBoxes()
        npc.destroy()
    }
}
