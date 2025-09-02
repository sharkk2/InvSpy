package org.invspy

import org.bukkit.plugin.java.JavaPlugin
import org.invspy.CheckInv
import org.invspy.Events

class InvSpy : JavaPlugin() {
    override fun onEnable() {
        logger.info("Loading")
        saveDefaultConfig()
        getCommand("checkinv")?.setExecutor(CheckInv())
        server.pluginManager.registerEvents(Events(), this)
        logger.info("Loaded")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
