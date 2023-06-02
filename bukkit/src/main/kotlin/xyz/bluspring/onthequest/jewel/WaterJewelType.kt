package xyz.bluspring.onthequest.jewel

import io.papermc.paper.event.entity.ElderGuardianAppearanceEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Guardian
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.seconds

class WaterJewelType(id: NamespacedKey, modelId: Int, slots: List<EquipmentSlot>, probability: Double) : EventedJewelType(id, modelId, slots, effectsWhenHeld = true, probability = probability) {
    @EventHandler
    fun onPlayerMove(ev: PlayerMoveEvent) {
        if (ev.player.eyeLocation.block.type != Material.WATER)
            return

        if (!JewelEffectEventHandler.containsJewel(ev.player, this))
            return

        ev.player.addPotionEffects(listOf(
            PotionEffect(PotionEffectType.WATER_BREATHING, 4.seconds.ticks, 15, true, false),
            PotionEffect(PotionEffectType.NIGHT_VISION, 4.seconds.ticks, 15, true, false),
            PotionEffect(PotionEffectType.FAST_DIGGING, 2.seconds.ticks, 2, true, false)
        ))
    }

    @EventHandler
    fun onPlayerAffectedByElderGuardian(ev: ElderGuardianAppearanceEvent) {
        if (!JewelEffectEventHandler.containsJewel(ev.affectedPlayer, this))
            return

        ev.isCancelled = true
    }

    @EventHandler
    fun onPlayerDamagedByGuardian(ev: EntityDamageByEntityEvent) {
        val entity = ev.entity

        if (entity !is Player)
            return

        if (!JewelEffectEventHandler.containsJewel(entity, this))
            return

        if (ev.damager is Guardian)
            ev.isCancelled = true
    }
}