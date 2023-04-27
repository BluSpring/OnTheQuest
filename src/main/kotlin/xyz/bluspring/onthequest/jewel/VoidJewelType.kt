package xyz.bluspring.onthequest.jewel

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.events.JewelEffectEventHandler

class VoidJewelType(id: NamespacedKey, modelId: Int, slots: List<EquipmentSlot>) : EventedJewelType(id, modelId, slots, effectsWhenHeld = true) {
    @EventHandler
    fun onPlayerGetEffect(ev: EntityPotionEffectEvent) {
        val entity = ev.entity

        if (entity !is Player)
            return

        if (JewelEffectEventHandler.getActiveJewels(entity)?.contains(this) != true)
            return

        if (ev.action != EntityPotionEffectEvent.Action.ADDED)
            return

        when (ev.modifiedType) {
            PotionEffectType.POISON, PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS, PotionEffectType.HARM -> {
                ev.isCancelled = true
            }
        }
    }
}