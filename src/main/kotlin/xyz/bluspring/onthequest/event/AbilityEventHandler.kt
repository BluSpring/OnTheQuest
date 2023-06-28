package xyz.bluspring.onthequest.event

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockCanBuildEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerEvent
import xyz.bluspring.onthequest.data.QuestRegistries

class AbilityEventHandler : Listener {
    @EventHandler
    fun onAllEvents(event: Event) {
        // If any player-related event that is to be handled by an ability isn't handled here,
        // just add it in.

        val player: Player = when (event) {
            // Entity events
            is EntityEvent -> {
                if (event.entity !is Player)
                    return

                event.entity as Player
            }
            is PlayerEvent -> event.player

            // Block events
            is BlockCanBuildEvent -> event.player ?: return
            is BlockBreakEvent -> event.player
            is BlockPlaceEvent -> event.player
            is TNTPrimeEvent -> {
                if (event.primerEntity is Player)
                    event.primerEntity as Player
                else return
            }

            // Inventory events
            is InventoryOpenEvent -> event.player as Player
            is InventoryInteractEvent -> event.whoClicked as Player
            is InventoryCloseEvent -> event.player as Player
            is EnchantItemEvent -> event.enchanter
            is PrepareItemEnchantEvent -> event.enchanter

            else -> return
        }

        // Run this on all available ability types
        QuestRegistries.ABILITY_TYPE.forEach { abilityType ->
            abilityType.all().forEach { ability ->
                // Check if the ability can be triggered for this event.
                if (ability.canTriggerForEvent(player, event)) {
                    // Check if the trigger causes a cooldown.
                    if (ability.triggerForEvent(player, event)) {
                        ability.triggerCooldown(player)
                    }
                }
            }
        }
    }
}