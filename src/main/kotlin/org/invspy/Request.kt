package org.invspy

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.bukkit.Bukkit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.ChatColor
import org.bukkit.Sound

object Request {
    private val pending = mutableMapOf<UUID, CompletableFuture<Boolean>>()
    fun requestPlayer(player: Player, requester: Player, timeoutSec: Int): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        pending[player.uniqueId] = future
        val allow = Component.text("${ChatColor.GREEN}[Allow]")
            .clickEvent(ClickEvent.callback {
                complete(player.uniqueId, true)
            })

        val deny = Component.text("${ChatColor.RED}[Deny]")
            .clickEvent(ClickEvent.callback {
                complete(player.uniqueId, false)
            })

        player.sendMessage(
            Component.text("${ChatColor.YELLOW}${requester.name}${ChatColor.RESET} requests to search your inventory\n${ChatColor.GRAY}You have $timeoutSec seconds to respond\n")
                .append(allow).append(Component.text(" | "))
                .append(deny)
        )
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)


        Bukkit.getScheduler().runTaskLater(
            Bukkit.getPluginManager().getPlugin("InvSpy")!!,
            Runnable {
                if (!future.isDone) {
                    complete(player.uniqueId, null)
                }
            },
            timeoutSec.toLong() * 20
        )
        return future
    }

    private fun complete(uuid: UUID, result: Boolean?) {
        val future = pending.remove(uuid) ?: return
        if (!future.isDone) {
            future.complete(result)
        }
    }

    fun hasRequest(player: Player): Boolean {
        return pending.containsKey(player.uniqueId)
    }
}
