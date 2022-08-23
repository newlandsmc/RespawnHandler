package me.cookie.menu

import me.cookie.data.CORPSE_PICKEDUP_NOSOULS
import me.cookie.data.CORPSE_PICKEDUP_SOULS
import me.cookie.data.cachedCorpses
import me.cookie.cookiecore.PlayerMenuUtility
import me.cookie.cookiecore.formatMinimessage
import me.cookie.cookiecore.gui.Menu
import me.cookie.cookiecore.gui.SlotsType
import me.cookie.data.souls
import me.cookie.traits.CorpseTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class CorpseInventory(
    playerMenuUtility: PlayerMenuUtility,
    private val corpse: NPC,
    private val itemstacks: List<ItemStack>,
    private val corpseNameFormat: String
): Menu(playerMenuUtility) {

    var corpseTrait: CorpseTrait = corpse.getOrAddTrait(CorpseTrait::class.java)

    val player = playerMenuUtility.player

    override val menuName: Component
        get() = corpseNameFormat
            .replace(
                "(corpseName)",
                corpseTrait.ownerName
            )
            .formatMinimessage()
    override val slots: SlotsType
        get() = SlotsType.CHEST_45

    init {
        player.souls += corpseTrait.souls
        if(corpseTrait.souls == 0){
            player.sendMessage(
                CORPSE_PICKEDUP_NOSOULS.replace(
                    "(corpseName)",
                    corpseTrait.ownerName
                )
                    .replace(
                        "(corpseSouls)",
                        corpseTrait.souls.toString()
                    )
                    .formatMinimessage()
            )
        }else{
            player.sendMessage(
                CORPSE_PICKEDUP_SOULS.replace(
                    "(corpseName)",
                    corpseTrait.ownerName
                )
                    .replace(
                        "(corpseSouls)",
                        corpseTrait.souls.toString()
                    )
                    .formatMinimessage()
            )
        }

    }

    override fun handleClick(e: InventoryClickEvent) {
        return
    }

    override fun handleClose(e: InventoryCloseEvent) {
        if(e.reason == InventoryCloseEvent.Reason.OPEN_NEW) return
        if(corpse.storedLocation == null) return
        val storedLoc = corpse.storedLocation.clone()
        e.inventory.contents.forEach {
            if (it != null) {
                storedLoc.world.dropItem(storedLoc, it)
            }
        }
        object: BukkitRunnable() {
            override fun run() {
                corpseTrait.ownerUUID.cachedCorpses = corpseTrait.ownerUUID.cachedCorpses.filter { it != corpse }
                corpse.getOrAddTrait(CorpseTrait::class.java).destroyCorpse()
            }
        }.runTaskLater(CitizensAPI.getPlugin(), 2)
    }

    override fun setMenuItems() {
        inventory.contents = itemstacks.toTypedArray()
    }
}
