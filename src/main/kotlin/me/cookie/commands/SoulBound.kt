package me.cookie.commands

import me.cookie.cookiecore.formatMinimessage
import me.cookie.cookiecore.openMenu
import me.cookie.menu.SoulBounder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SoulBound: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender !is Player) {
            sender.sendMessage("<red>You must be a player to use this command!".formatMinimessage())
            return true
        }
        sender.openMenu(SoulBounder::class.java)
        return true
    }
}