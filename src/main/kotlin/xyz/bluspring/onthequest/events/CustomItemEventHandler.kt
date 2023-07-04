package xyz.bluspring.onthequest.events

import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import xyz.bluspring.onthequest.data.item.CustomItemManager

class CustomItemEventHandler : Listener {
    @EventHandler
    fun onItemUse(ev: PlayerInteractEvent) {
        if (!ev.hasItem())
            return

        val player = ev.player
        val item = ev.item!!

        val customItem = CustomItemManager.getCustomItem((item as CraftItemStack).handle) ?: return
        customItem.use(player, ev)
    }
}