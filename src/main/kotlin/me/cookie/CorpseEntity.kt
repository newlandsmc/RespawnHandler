package me.cookie

import me.cookie.cookiecore.gui.SlotsType
import me.cookie.cookiecore.serialize
import me.cookie.traits.CorpseTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.HologramTrait
import net.citizensnpcs.trait.SkinTrait
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit.createInventory
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CorpseEntity(
    private val player: Player,
    private val items: List<ItemStack>,
    ) {

    private var npc: NPC
    private var inventory: Inventory
    init{
        npc = createNPC()
        inventory = createInventory(null, SlotsType.CHEST_45.size, player.name()
            .append(Component.text("'s corpse").color(NamedTextColor.RED)))
    }
    private fun createNPC(): NPC {
        return CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "").apply {
            getOrAddTrait(SkinTrait::class.java).skinName = player.name
            getOrAddTrait(CorpseTrait::class.java).apply {
                ownerName = player.name
                ownerUUID = player.uniqueId
                itemstacks = items.serialize()
                timeSpawned = System.currentTimeMillis()
                maxCorpses = CORPSE_LIMIT
                gracePeriod = CORPSE_GRACE_PERIOD * 60000 // convert to ms
                decayTime = CORPSE_DECAY_TIME * 60000
                nameFormat = CORPSE_NAME
                corpseLockedMessage = CORPSE_LOCKED_MESSAGE
                souls = player.souls
            }

            getOrAddTrait(HologramTrait::class.java).apply {
                setLine(0, "loading...")
            }
            setAlwaysUseNameHologram(true)
            data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, "false") // hide default name
        }
    }

    fun spawnCorpse(loc: Location){
        npc.spawn(
            loc.apply {
                pitch = 0f
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
        player.souls = 0
    }
}