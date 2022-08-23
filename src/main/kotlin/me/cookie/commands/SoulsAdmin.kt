package me.cookie.commands

import me.cookie.cookiecore.NO_PERMISSION
import me.cookie.cookiecore.formatMinimessage
import me.cookie.data.souls
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SoulsAdmin: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(!sender.hasPermission("respawnhandler.admin")) {
            sender.sendMessage(NO_PERMISSION)
            return true
        }
        if(args.isEmpty()) {
            sender.sendMessage("<red>Usage: /soulsadmin [ set | add | list ] <player> <souls>".formatMinimessage())
            return true
        }
        if(args.size < 2) {
            sender.sendMessage("<red>Usage: /soulsadmin [ set | add | list ] <player> <souls>".formatMinimessage())
            return true
        }
        val player = sender.server.getPlayer(args[1])
        if (player == null) {
            sender.sendMessage("<red>Player not found!".formatMinimessage())
            return true
        }
        when(args[0]) {
            "set" -> {
                try {
                    val souls = args[2].toInt()
                    player.souls = souls
                    sender.sendMessage("<green>Set ${player.name}'s souls to $souls".formatMinimessage())
                } catch (e: NumberFormatException) {
                    sender.sendMessage("<red>Invalid number!".formatMinimessage())
                }

            }
            "add" -> {
                try {
                    val souls = args[2].toInt()
                    player.souls += souls
                    sender.sendMessage("<green>Added $souls souls to ${player.name}".formatMinimessage())
                } catch (e: NumberFormatException) {
                    sender.sendMessage("<red>Invalid number!".formatMinimessage())
                }
            }
            "list" -> {
                try {
                    val souls = player.souls
                    sender.sendMessage("<green>${player.name} has $souls souls".formatMinimessage())
                } catch (e: NumberFormatException) {
                    sender.sendMessage("<red>Invalid number!".formatMinimessage())
                }
            }
            else -> {
                if(sender !is Player) {
                    sender.sendMessage("<red>You must be a player to use this command!".formatMinimessage())
                    return true
                }
                val elsePlayer = Bukkit.getPlayer(args[0])
                if(elsePlayer != null) {
                    sender.sendMessage("<green>${elsePlayer.name}'s souls: ${elsePlayer.souls}".formatMinimessage())
                    return true
                }
                try {
                    val souls = args[0].toInt()
                    sender.souls = souls
                    sender.sendMessage("<green>Set your souls to $souls".formatMinimessage())
                } catch (e: NumberFormatException) {
                    sender.sendMessage("<red>Invalid number!".formatMinimessage())
                }
            }
        }
        return true
    }
}
