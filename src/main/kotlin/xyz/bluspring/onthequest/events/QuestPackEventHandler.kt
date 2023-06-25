package xyz.bluspring.onthequest.events

import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import xyz.bluspring.onthequest.data.QuestDatapackManager

class QuestPackEventHandler : Listener {
    @EventHandler
    fun onResourceReload(ev: ServerResourcesReloadedEvent) {
        QuestDatapackManager.loadAllResources()
    }
}