package xyz.bluspring.onthequest.events

import com.destroystokyo.paper.event.block.TNTPrimeEvent
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockCanBuildEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
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
            } else {
                ability.resetEffects(player)
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
            } else {
                ability.resetEffects(ev.player)
            }
        }
    }

    // TODO: write a way to load all of the events because fuck you bukkit
    @EventHandler
    fun fuck(ev: EntityDamageEvent) {}

    @EventHandler
    fun fuck(ev: PlayerToggleSneakEvent) {}

    @EventHandler
    fun fuck(ev: PlayerInteractEntityEvent) {}

    @EventHandler
    fun fuck(ev: EntityPotionEffectEvent) {}

    @EventHandler
    fun fuck(ev: BlockDropItemEvent) {}

    @EventHandler
    fun fuck(ev: EntityDropItemEvent) {}
}