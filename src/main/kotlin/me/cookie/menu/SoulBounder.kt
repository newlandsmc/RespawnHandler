package me.cookie.menu

import me.cookie.RespawnHandler
import me.cookie.SOULBOUND_COST
import me.cookie.cookiecore.ItemStackBuilder
import me.cookie.cookiecore.PlayerMenuUtility
import me.cookie.cookiecore.formatMinimessage
import me.cookie.cookiecore.gui.Menu
import me.cookie.cookiecore.gui.SlotsType
import me.cookie.souls
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SoulBounder(playerMenuUtility: PlayerMenuUtility) : Menu(playerMenuUtility) {
    override val menuName: Component
        get() = Component.text("Soul Bounder")
    override val slots: SlotsType
        get() = SlotsType.CHEST_27

    val player = playerMenuUtility.player
    private val plugin = RespawnHandler.instance

    override fun handleClick(e: InventoryClickEvent) {
        if(e.clickedInventory == null) return
        val clickedInventory = e.clickedInventory!!
        val currentItem = e.currentItem ?: return
        if(currentItem.type == Material.AIR) return
        if(currentItem.amount > 1) {
            e.isCancelled = true
            player.sendMessage("<gray>You can't soul bind more than one item at a time!".formatMinimessage())
            return
        }
        if(clickedInventory.type == InventoryType.PLAYER && inventory.getItem(13) != null) {
            e.isCancelled = true
            player.sendMessage("<gray>You can't soul bind more than one item at a time!".formatMinimessage())
            return
        }
        if (e.slot != 13 && e.clickedInventory!!.type != InventoryType.PLAYER) {
            e.isCancelled = true
            val item = inventory.getItem(13) ?: return
            if(item.type == Material.AIR) return
            val soulbounded = item.itemMeta.persistentDataContainer
                .has(NamespacedKey(plugin, "soulbounded"), PersistentDataType.BYTE)

            if(soulbounded) return

            if(player.souls < SOULBOUND_COST) {
                player.sendMessage("<red>You don't have enough souls to soulbind this item!".formatMinimessage())
                return
            }

            val meta = item.itemMeta

            if(meta.hasLore()) {
                val lore = meta.lore()
                lore!!.add("<gray>[Soulbounded]".formatMinimessage()
                    .decoration(TextDecoration.ITALIC, false)
                )
                meta.lore(lore)
            } else {
                val lore = mutableListOf<Component>()
                lore.add("<gray>[Soulbounded]".formatMinimessage()
                    .decoration(TextDecoration.ITALIC, false)
                )
                meta.lore(lore)
            }

            player.souls -= SOULBOUND_COST

            meta.persistentDataContainer
                .set(NamespacedKey(plugin, "soulbounded"), PersistentDataType.BYTE, 1)


            item.itemMeta = meta

            player.sendMessage("<green>Successfully soulbound item!".formatMinimessage())

            inventory.setItem(26,
                createSoulsInfoItem()
            )

            return
        }
        return
    }

    override fun handleClose(e: InventoryCloseEvent) {
        val slot13 = e.inventory.getItem(13) ?: return
        val player = e.player
        if(slot13.type == Material.AIR) return

        player.inventory.addItem(slot13)
    }

    override fun setMenuItems() {
        filler(ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        inventory.setItem(13, ItemStack(Material.AIR))
        inventory.setItem(22,
            ItemStackBuilder(Material.ENCHANTED_BOOK)
                .withName(
                    Component.text("Apply Soulbound", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
                )
                .withLore(
                    Component.text("Costs: $SOULBOUND_COST souls", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                )
                .build()
        )
        inventory.setItem(26,
            createSoulsInfoItem()
        )

        return
    }

    private fun createSoulsInfoItem(): ItemStack {
        return ItemStackBuilder(Material.BOOK)
            .withName(
                Component.text("Souls: ${player.souls}", NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
            )
            .withLore(
                Component.text("Applies soulbound to the item", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("in the middle slot.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("This item will stay", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("in your inventory even after death.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
            ).build()
    }
}