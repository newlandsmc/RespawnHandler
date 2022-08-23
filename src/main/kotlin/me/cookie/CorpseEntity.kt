package me.cookie

import me.cookie.cookiecore.formatMinimessage
import me.cookie.cookiecore.gui.SlotsType
import me.cookie.cookiecore.serialize
import me.cookie.data.*
import me.cookie.traits.CorpseTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.HologramTrait
import net.citizensnpcs.trait.SkinTrait
import org.bukkit.Bukkit
import org.bukkit.Bukkit.createInventory
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class CorpseEntity(
    private val playerName: String,
    private val playerUUID: UUID,
    private val playerSouls: Int,
    private val items: List<ItemStack>,
    private val corpse: Corpse
    ) {

    private val npc: NPC
    private val inventory: Inventory
    init{
        npc = createNPC()
        inventory = createInventory(
            null,
            SlotsType.CHEST_45.size,
            playerName.plus("'s Corpse").formatMinimessage()
        )
    }
    private fun createNPC(): NPC {
        return CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "").apply {
            getOrAddTrait(SkinTrait::class.java).skinName = playerName
            getOrAddTrait(CorpseTrait::class.java).apply {
                ownerName = playerName
                ownerUUID = playerUUID
                itemstacks = items.serialize()
                timeSpawned = System.currentTimeMillis()
                maxCorpses = CORPSE_LIMIT
                gracePeriod = CORPSE_GRACE_PERIOD * 60000 // convert to ms
                decayTime = CORPSE_DECAY_TIME * 60000
                nameFormat = CORPSE_NAME
                corpseLockedMessage = CORPSE_LOCKED_MESSAGE
                souls = playerSouls
                id = corpse.id
            }

            getOrAddTrait(HologramTrait::class.java).apply {
                setLine(0, "loading...")
            }
            setAlwaysUseNameHologram(true)
            data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, "false") // hide default name
        }
    }

    fun spawnCorpse(loc: Location): NPC{
        npc.spawn(
            loc.apply {
                pitch = 0f
                yaw = -yaw - 90f
            }
        )
        npc.entity.isCustomNameVisible = false
        (npc.entity as Player).addPotionEffect(
            PotionEffect(
                PotionEffectType.INVISIBILITY,
                20,
                0,
                false,
                false,
                false
            )
        )
        //player.souls = 0
        Bukkit.getPlayer(playerUUID)?.souls = 0;
        return npc
    }
}
