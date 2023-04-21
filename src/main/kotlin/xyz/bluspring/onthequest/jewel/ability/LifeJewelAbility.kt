package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.minutes

class LifeJewelAbility : JewelAbility(
    JewelAbilities.key("life"),
    30.minutes.inWholeMilliseconds
) {
    @EventHandler
    fun onPlayerDamage(ev: EntityDamageEvent) {
        val entity = ev.entity

        if (entity !is Player)
            return

        if (!doesAbilityApply(entity))
            return

        if (entity.health - ev.finalDamage <= 6) {
            run(entity)
        }
    }

    override fun runAbility(player: Player): Boolean {
        player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 5.minutes.ticks, 2))
        return true
    }
}