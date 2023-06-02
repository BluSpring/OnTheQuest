package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class WaterJewelAbility : JewelAbility(
    JewelAbilities.key("water_primary"),
    15.minutes.inWholeMilliseconds
) {
    @EventHandler
    fun onPlayerRightClick(ev: PlayerInteractEvent) {
        if (!doesAbilityApply(ev.player))
            return

        if (this.run(ev.player)) {
            ev.isCancelled = true
        }
    }

    override fun runAbility(player: Player): Boolean {
        player.addPotionEffects(listOf(
            PotionEffect(PotionEffectType.ABSORPTION, 30.seconds.ticks, 2),
            PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30.seconds.ticks, 2)
        ))

        player.world.spawnParticle(Particle.BUBBLE_POP,
            player.location.x, player.location.y + 0.35, player.location.z,
            50, 0.7, 0.2, 0.7, 0.01
        )

        return true
    }
}