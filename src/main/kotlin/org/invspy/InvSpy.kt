package org.invspy

import org.bukkit.plugin.java.JavaPlugin
import org.invspy.CheckInv

class InvSpy : JavaPlugin() {
    override fun onEnable() {
        getCommand("checkinv")?.setExecutor(CheckInv())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
