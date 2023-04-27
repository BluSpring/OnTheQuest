package xyz.bluspring.onthequest.jewel

import io.papermc.paper.event.entity.ElderGuardianAppearanceEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.seconds

class WaterJewelType(id: NamespacedKey, modelId: Int, slots: List<EquipmentSlot>) : EventedJewelType(id, modelId, slots, effectsWhenHeld = true) {
    @EventHandler
    fun onPlayerMove(ev: PlayerMoveEvent) {
        if (ev.player.eyeLocation.block.type != Material.WATER)
            return

        if (JewelEffectEventHandler.getActiveJewels(ev.player)?.contains(this) != true)
            return

        ev.player.addPotionEffects(listOf(
            PotionEffect(PotionEffectType.WATER_BREATHING, 4.seconds.ticks, 15, true, false),
            PotionEffect(PotionEffectType.NIGHT_VISION, 4.seconds.ticks, 15, true, false),
            PotionEffect(PotionEffectType.FAST_DIGGING, 2.seconds.ticks, 2, true, false)
        ))
    }

    @EventHandler
    fun onPlayerAffectedByElderGuardian(ev: ElderGuardianAppearanceEvent) {
        if (JewelEffectEventHandler.getActiveJewels(ev.affectedPlayer)?.contains(this) != true)
            return

        ev.isCancelled = true
    }
}