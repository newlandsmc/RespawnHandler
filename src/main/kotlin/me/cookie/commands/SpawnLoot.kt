package me.cookie.commands

// Old system

/*
import com.griefcraft.cache.BlockCache
import com.griefcraft.lwc.LWC
import com.griefcraft.model.Protection
import me.goldencookie.deathItems
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class SpawnLoot(private val plugin: JavaPlugin, private val lwc: LWC): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) return true // Players cannot execute this command.

        if (args.isEmpty()) {
            plugin.logger.log(Level.SEVERE, "Usage: /spawnloot <player name>")
            return true
        }

        val target = Bukkit.getPlayer(args[0])

        if (target == null) {
            plugin.logger.log(Level.SEVERE, "Player not found: ${args[0]}")
            return true
        }

        target.inventory.clear()

        target.teleport(
            target.location.clone().apply {
                x = 53.0
                y = 64.0
                z = 15.0
            }
        )

        if(target.deathItems.isEmpty()){
            plugin.logger.log(Level.INFO, "Player ${target.name} has no death items!")
            return true
        }

        // Temporary locations for the chests.
        val leftBlock: Block = Location(
            Bukkit.getWorld("world"),
            target.location.x - 2.0,
            target.location.y,
            target.location.z + 0.0
        ).block

        val rightBlock: Block = Location(
            Bukkit.getWorld("world"),
            target.location.x - 2.0,
            target.location.y,
            target.location.z + 1.0
        ).block

        leftBlock.type = Material.CHEST
        rightBlock.type = Material.CHEST


        // Using discrete imports to differentiate between chest data and chest block.

        val left = Material.CHEST.createBlockData() as org.bukkit.block.data.type.Chest
        val right = Material.CHEST.createBlockData() as org.bukkit.block.data.type.Chest

        left.type = org.bukkit.block.data.type.Chest.Type.LEFT
        right.type = org.bukkit.block.data.type.Chest.Type.RIGHT

        left.facing = BlockFace.EAST
        right.facing = BlockFace.EAST

        leftBlock.blockData = left
        rightBlock.blockData = right

        val leftChest = leftBlock.state as org.bukkit.block.Chest
        val rightChest = rightBlock.state as org.bukkit.block.Chest

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

        val items = listOf(
            *target.deathItems.toTypedArray()
        )

        // make sure there are items in the list
        if (items.isEmpty()) {
            plugin.logger.log(Level.SEVERE, "Player ${target.name} has no items in their inventory.")
            return true
        }

        // take the first 27 items and place them in the right chest
        for (i in 0 until 26) {
            // make sure that item 'i' exists in the list
            if (items.size > i) {
                rightChest.blockInventory.addItem(items[i])
            }
        }

        // make sure there are enough items in the list
        if (items.size > 27) {
            // take the remaining items and place them in the left chest
            for (i in 27 until items.size) {
                leftChest.blockInventory.setItem(i - 27, items[i])
            }
        }

        target.teleport(
            target.location.clone().apply {
                yaw = 90f
                pitch = 40f
                z += 1.0
            }
        )

        return true
    }
}*/
