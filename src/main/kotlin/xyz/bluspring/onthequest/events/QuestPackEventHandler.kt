package xyz.bluspring.onthequest.events

import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import xyz.bluspring.onthequest.data.QuestDatapackManager
import java.util.concurrent.atomic.AtomicBoolean

class QuestPackEventHandler : Listener {
    @EventHandler
    fun onResourceReload(ev: ServerResourcesReloadedEvent) {
        if (MARK_HAS_RELOADED.get()) {
            MARK_HAS_RELOADED.set(false)
            return
        }

        MARK_HAS_RELOADED.set(true)
        QuestDatapackManager.reload()
    }

    companion object {
        // A lock to ensure the resources don't get stuck in a reload loop.
        private val MARK_HAS_RELOADED = AtomicBoolean(false)
    }
}