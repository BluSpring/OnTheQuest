package xyz.bluspring.onthequest

import org.bukkit.plugin.java.JavaPlugin
import xyz.bluspring.onthequest.events.*

class OnTheQuest : JavaPlugin() {
    override fun onEnable() {
        plugin = this

        this.server.pluginManager.registerEvents(CustomMapEventHandler(), this)
        this.server.pluginManager.registerEvents(JewelEffectEventHandler(), this)
    }

    override fun onDisable() {

    }

    companion object {
        lateinit var plugin: OnTheQuest
        val debug = System.getProperty("otq.debugMode", "false") == "true"
    }
}