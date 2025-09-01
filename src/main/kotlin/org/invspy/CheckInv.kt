package org.invspy

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import kotlin.math.abs

class CheckInv : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}Only players can use this command")
            return true
        }

        if (args.size != 1) {
            sender.sendMessage("${ChatColor.RED}Usage: /<command> <player>")
            return true
        }

        val plr: Player? = Bukkit.getPlayer(args[0])
        if (plr == null || !plr.isOnline()) {
            sender.sendMessage("${ChatColor.RED}${args[0]} is not online")
            return true
        }
        val size = plr.inventory.size
        val nearest = ((size + 8) / 9) * 9
        val safeSize = nearest.coerceIn(9, 54)
        val invui: Inventory = Bukkit.createInventory(null, safeSize, Component.text("${plr.name}'s inventory"))
        invui.contents = plr.inventory.contents
        sender.openInventory(invui)
        return true
    }
}
