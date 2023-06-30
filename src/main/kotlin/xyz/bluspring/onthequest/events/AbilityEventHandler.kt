package xyz.bluspring.onthequest.events

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import org.bukkit.entity.Player
import org.bukkit.event.*
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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.RegisteredListener
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.jewel.Jewel
import xyz.bluspring.onthequest.data.jewel.JewelManager
import xyz.bluspring.onthequest.data.util.KeybindType

class AbilityEventHandler : Listener {
    init {
        val listener = RegisteredListener(this, { _, event ->
            onAllEvents(event)
        }, EventPriority.NORMAL, OnTheQuest.plugin, false)

        HandlerList.getHandlerLists().forEach {
            it.register(listener)
        }
    }

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

        val jewel = JewelManager.getOrCreateJewel(player)
        val level = JewelManager.getOrCreateLevel(player)

        jewel.getPassiveAbilitiesInLevelRange(level).forEach { ability ->
            // Check if the ability can be triggered for this event.
            if (ability.canTriggerForEvent(player, event)) {
                // Check if the trigger causes a cooldown.
                if (ability.triggerForEvent(player, event)) {
                    ability.triggerParticles(player)
                    ability.triggerCooldown(player)
                }
            }
        }
    }

    @EventHandler
    fun onClickEvent(ev: PlayerInteractEvent) {
        val keybindType = KeybindType.get(ev.action.isLeftClick, ev.player.isSneaking)

        if (!ev.hasItem())
            return

        val player = ev.player
        val heldItem = ev.item!!

        if (!heldItem.hasItemMeta())
            return

        if (!heldItem.itemMeta.persistentDataContainer.has(Jewel.JEWEL_TYPE_KEY))
            return

        val jewel = JewelManager.getOrCreateJewel(player)
        val level = JewelManager.getOrCreateLevel(player)

        jewel.getActiveAbilitiesInLevelRange(level).forEach { ability ->
            if (ability.keybindType != keybindType)
                return@forEach

            if (ability.canTrigger(ev.player)) {
                if (ability.trigger(ev.player, ev.interactionPoint)) {
                    ability.triggerParticles(ev.player)
                    ability.triggerCooldown(ev.player)
                }
            }
        }
    }
}