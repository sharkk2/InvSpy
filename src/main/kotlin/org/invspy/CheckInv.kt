package org.invspy

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.*

class CheckInv(private val plugin: JavaPlugin) : CommandExecutor {
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

        val isOp = sender.isOp()
        val opReqBypass = plugin.config.getBoolean("op_bypass_request")
        val opOnly = plugin.config.getBoolean("invsearch_op_only")
        val closeRangeSet = plugin.config.getBoolean("close_range")
        val reqRange = plugin.config.getInt("block_range")
        val closeRangeBypass = plugin.config.getBoolean("close_range_request_bypass")
        val reqTime = plugin.config.getInt("request_timeout")
        if (opOnly && !isOp) {
            sender.sendMessage("${ChatColor.RED}Only operators can use this command")
            return true
        }
        if (opOnly) {
            openInventory(sender, plr)
            return true
        }

        val noRequest = plugin.config.getBoolean("no_request")
        if (noRequest || (opReqBypass && isOp)) {
            if (closeRangeSet && !isOp) {
                val distance:Double = getDistance(sender, plr)
                if (distance > reqRange) {
                    sender.sendMessage("${ChatColor.RED} You are too far from ${plr.name}")
                    return true
                }
            }
            openInventory(sender, plr)
            if (!isOp) {plr.sendMessage("${ChatColor.YELLOW}${sender.name}${ChatColor.GRAY} is currently searching your inventory")}
            return true
        }

        if (closeRangeSet) {
            val distance:Double = getDistance(sender, plr)
            if (distance > reqRange) {
                sender.sendMessage("${ChatColor.RED} You are too far from ${plr.name}")
                return true
            } else {
                if (closeRangeBypass) {
                    openInventory(sender, plr)
                    if (!isOp) {plr.sendMessage("${ChatColor.YELLOW}${sender.name}${ChatColor.GRAY} is currently searching your inventory")}
                    return true
                }
            }
        }

        if (Request.hasRequest(plr)) {
            sender.sendMessage("${ChatColor.RED}${plr.name} already has a pending request")
            return true
        }
        sender.sendMessage("A search request has been sent to ${ChatColor.YELLOW}${plr.name}\n${ChatColor.GRAY}Request timeouts in $reqTime seconds")
        Request.requestPlayer(plr, sender, reqTime).thenAccept { result ->
            when (result) {
                true  -> openInventory(sender, plr)
                false -> sender.sendMessage("${ChatColor.RED}${plr.name} has denied your search request")
                null  -> sender.sendMessage("${ChatColor.RED}${plr.name} did not respond to your search request")
            }
        }
        return true
    }

    fun openInventory(player: Player, target: Player) {
        val hiddenItems = plugin.config.getStringList("hidden_items")
        val opSeeAll = plugin.config.getBoolean("op_hidden_items_bypass")
        val hiddenMats = mutableListOf<Material>()
        for (id in hiddenItems) {
            val material = Material.matchMaterial(id)
            if (material != null) {
                hiddenMats.add(material)
            }
        }

        val invui: Inventory = Bukkit.createInventory(null, 54, Component.text("${target.name}'s inventory (InvSpy)"))
        if (!opSeeAll) {
            invui.contents = target.inventory.contents.filterNotNull().filter {!hiddenMats.contains(it.type)}.toTypedArray()
        } else {
            invui.contents = target.inventory.contents
        }
        player.openInventory(invui)
    }

    fun getDistance(player1: Player, player2: Player): Double {
        //  sqrt((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)
        //return sqrt((player2.x - player1.x).pow(2) + (player2.y - player1.y).pow(2) + (player2.z - player1.z).pow(2))
        if (player1.world != player2.world) return Double.MAX_VALUE
        return player1.location.distance(player2.location)
    }
}
