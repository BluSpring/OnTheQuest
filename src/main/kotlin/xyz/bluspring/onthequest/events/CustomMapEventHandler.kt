package xyz.bluspring.onthequest.events

import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import xyz.bluspring.onthequest.generation.MapChestManager

class CustomMapEventHandler : Listener {
    @EventHandler
    fun onMapInteract(ev: PlayerInteractEvent) {
        if (!ev.action.isRightClick || !ev.hasItem())
            return

        val item = ev.item!!

        if (item.type != Material.MAP)
            return

        if (item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData == 16) {
            ev.setUseItemInHand(Event.Result.ALLOW)

            val stack = MapChestManager.generate(ev.player)
            ev.player.inventory.setItem(ev.hand ?: EquipmentSlot.HAND, stack)
        }
    }
}