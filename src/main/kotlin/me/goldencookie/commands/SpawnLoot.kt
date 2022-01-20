package me.goldencookie.commands

import com.griefcraft.cache.BlockCache
import com.griefcraft.lwc.LWC
import com.griefcraft.model.Protection
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.Chest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class SpawnLoot(private val plugin: JavaPlugin, val lwc: LWC): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) return true // Players cannot execute this command.

        if(args.isEmpty()){
            plugin.logger.log(Level.SEVERE, "Usage: /spawnloot <player name>")
            return true
        }

        val target = Bukkit.getPlayer(args[0])

        if(target == null){
            plugin.logger.log(Level.SEVERE, "Player does not exist!")
            return true
        }

        val leftBlock: Block = Location(Bukkit.getWorld("world"), 1.0, 60.0, 1.0).block
        val rightBlock: Block = Location(Bukkit.getWorld("world"), 1.0, 60.0, 0.0).block

        leftBlock.type = Material.CHEST
        rightBlock.type = Material.CHEST

        val left = Material.CHEST.createBlockData() as Chest
        val right = Material.CHEST.createBlockData() as Chest

        left.type = Chest.Type.LEFT
        right.type = Chest.Type.RIGHT

        leftBlock.setBlockData(left, false)
        rightBlock.setBlockData(right, false)

        lwc.physicalDatabase.registerProtection(
            BlockCache.getInstance().getBlockId(leftBlock),
            Protection.Type.PRIVATE,
            "world",
            target.uniqueId.toString(),
            "",
            leftBlock.x, leftBlock.y, leftBlock.z
        )

        lwc.physicalDatabase.registerProtection(
            BlockCache.getInstance().getBlockId(rightBlock),
            Protection.Type.PRIVATE,
            "world",
            target.uniqueId.toString(),
            "",
            rightBlock.x, rightBlock.y, rightBlock.z
        )
        return true
    }
}