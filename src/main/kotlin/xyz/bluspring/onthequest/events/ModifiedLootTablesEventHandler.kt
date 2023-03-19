package xyz.bluspring.onthequest.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent
import xyz.bluspring.onthequest.generation.MapChestManager
import kotlin.random.Random

class ModifiedLootTablesEventHandler : Listener {
    private val random = Random(Random.nextLong())

    @EventHandler
    fun onLootGenerate(ev: LootGenerateEvent) {
        if (ev.isPlugin)
            return

        val probability = MapChestManager.LOOT_TABLE_CHANCES[ev.lootTable.key] ?: return
        val loot = ev.loot

        val rng = (1.0 / probability).toInt()
        if (random.nextInt(rng) == 0) {
            val count = random.nextInt(1, 2)

            loot.add(MapChestManager.getMapShard(count))
        }
    }
}