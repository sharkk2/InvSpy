package org.invspy

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory

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

        Request.requestPlayer(plr, sender, 20).thenAccept { result ->
            when (result) {
                true  -> openInventory(sender, plr)
                false -> sender.sendMessage("${ChatColor.RED}${plr.name} has denied your search request")
                null  -> sender.sendMessage("${ChatColor.RED}${plr.name} did not respond to your search request")
            }
        }

        return true
    }

    fun openInventory(player: Player, target: Player) {
        var size = target.inventory.size
        val nearest = ((size + 8) / 9) * 9
        size = nearest.coerceIn(9, 54)
        val invui: Inventory = Bukkit.createInventory(null, size, Component.text("${target.name}'s inventory"))
        invui.contents = target.inventory.contents
        target.openInventory(invui)
    }
}
