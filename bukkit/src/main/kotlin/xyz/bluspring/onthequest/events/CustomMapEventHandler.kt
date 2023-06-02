package xyz.bluspring.onthequest.events

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.loot.LootContext
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.generation.MapChestManager
import java.util.*

class CustomMapEventHandler : Listener {
    @EventHandler
    fun onMapInteract(ev: PlayerInteractEvent) {
        if (!ev.action.isRightClick || !ev.hasItem())
            return

        val item = ev.item!!

        if (item.type != Material.MAP)
            return

        if (item.itemMeta.hasCustomModelData()) {
            if (item.itemMeta.customModelData == 16) {
                if (ev.player.world.isFixedTime) {
                    ev.setUseItemInHand(Event.Result.DENY)
                    ev.player.sendActionBar(Component.text("The map doesn't seem to work here..."))
                    return
                }

                ev.setUseItemInHand(Event.Result.DENY)

                val stack = MapChestManager.generate(ev.player)

                if (item.amount > 1) {
                    ev.player.inventory.setItem(ev.hand ?: EquipmentSlot.HAND, item.subtract(1))
                    ev.player.inventory.addItem(stack)
                } else
                    ev.player.inventory.setItem(ev.hand ?: EquipmentSlot.HAND, stack)
            } else if (item.itemMeta.customModelData == 15) {
                // map shards
                ev.setUseItemInHand(Event.Result.DENY)
            }
        }
    }

    @EventHandler
    fun onJewelChestInteract(ev: PlayerInteractEvent) {
        if (ev.clickedBlock == null)
            return

        if (ev.clickedBlock!!.type != Material.CHEST)
            return

        val chest = ev.clickedBlock!!.getState(false) as Chest

        if (!chest.persistentDataContainer.has(MapChestManager.MAP_CHEST_UUID))
            return

        val uuid = chest.persistentDataContainer.get(MapChestManager.MAP_CHEST_UUID, PersistentDataType.STRING)

        var hasMap = false
        for (itemStack in ev.player.inventory) {
            if (itemStack == null)
                continue

            if (itemStack.type != Material.FILLED_MAP)
                continue

            if (itemStack.hasItemMeta() && itemStack.itemMeta.persistentDataContainer.has(MapChestManager.MAP_ID_KEY)) {
                if (itemStack.itemMeta.persistentDataContainer.get(MapChestManager.MAP_ID_KEY, PersistentDataType.STRING) == uuid) {
                    hasMap = true
                    break
                }
            }
        }

        if (!hasMap) {
            ev.isCancelled = true
            ev.player.sendActionBar(Component.text("You do not hold the key for this chest."))
            return
        }

        if (chest.inventory.isEmpty && !chest.persistentDataContainer.has(MapChestManager.MAP_CHEST_GENERATED)) {
            val context = LootContext.Builder(chest.location).build()
            MapChestManager.LOOT_TABLE.fillInventory(chest.inventory, Random(kotlin.random.Random.nextLong()), context)

            chest.persistentDataContainer.set(MapChestManager.MAP_CHEST_GENERATED, PersistentDataType.BYTE, 1)
        }
    }

    @EventHandler
    fun onChunkLoad(ev: ChunkLoadEvent) {
        MapChestManager.checkGenerationQueue(ev.chunk)
    }
}