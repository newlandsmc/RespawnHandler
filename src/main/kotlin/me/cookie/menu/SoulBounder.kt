package me.cookie.menu

import me.cookie.cookiecore.PlayerMenuUtility
import me.cookie.cookiecore.gui.Menu
import me.cookie.cookiecore.gui.SlotsType
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class SoulBound(playerMenuUtility: PlayerMenuUtility) : Menu(playerMenuUtility) {
    override val menuName: Component
        get() = Component.text("Soul Bounder")
    override val slots: SlotsType
        get() = SlotsType.CHEST_27

    override fun handleClick(e: InventoryClickEvent) {
        return
    }

    override fun handleClose(e: InventoryCloseEvent) {
        val slot13 = e.inventory.getItem(13) ?: return
        val player = e.player
        if(slot13.type == Material.AIR) return

        player.inventory.addItem(slot13)
    }

    override fun setMenuItems() {
        return
    }
}