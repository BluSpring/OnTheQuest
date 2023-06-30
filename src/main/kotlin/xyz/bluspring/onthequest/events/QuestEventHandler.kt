package xyz.bluspring.onthequest.events

import net.minecraft.resources.ResourceLocation
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent
import xyz.bluspring.onthequest.data.quests.QuestCustomCriterias

class QuestEventHandler : Listener {
    @EventHandler
    fun onLootGenerated(ev: LootGenerateEvent) {
        if (ev.entity == null)
            return

        if (ev.entity !is Player)
            return

        val player = (ev.entity as CraftPlayer).handle
        QuestCustomCriterias.PLAYER_OPEN_NEW_LOOT_CHEST.trigger(player, ResourceLocation(ev.lootTable.key.toString()))
    }
}