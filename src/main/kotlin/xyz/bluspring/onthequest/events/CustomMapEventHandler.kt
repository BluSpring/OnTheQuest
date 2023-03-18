package xyz.bluspring.onthequest.events

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.loot.LootContext
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.generation.JewelChestLootTable
import xyz.bluspring.onthequest.generation.MapChestManager
import xyz.bluspring.onthequest.generation.MapChestMetadata
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
                ev.setUseItemInHand(Event.Result.ALLOW)

                val stack = MapChestManager.generate(ev.player)
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

        if (!ev.clickedBlock!!.hasMetadata("questsmp_map_chest"))
            return

        val uuid = ev.clickedBlock!!.getMetadata("questsmp_map_chest")[0] as MapChestMetadata

        if (ev.player.inventory.none {
                it.type == Material.FILLED_MAP
                        && it.hasItemMeta()
                        && it.itemMeta.persistentDataContainer.has(MapChestManager.MAP_ID_KEY)
                        && it.itemMeta.persistentDataContainer.get(MapChestManager.MAP_ID_KEY, PersistentDataType.STRING) == uuid.toString()
        }) {
            ev.isCancelled = true
            ev.player.sendActionBar(Component.text("You do not hold the key for this chest."))
            return
        }

        val chest = ev.clickedBlock!!.state as Chest

        if (ev.action.isRightClick && chest.inventory.isEmpty && !chest.hasMetadata("questsmp_chest_generated")) {
            val context = LootContext.Builder(chest.location).build()
            MapChestManager.LOOT_TABLE.fillInventory(chest.inventory, Random(kotlin.random.Random.nextLong()), context)

            chest.setMetadata("questsmp_chest_generated", FixedMetadataValue(OnTheQuest.plugin, true))
        }
    }
}