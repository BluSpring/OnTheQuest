package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.minutes

class LifeJewelAbility : JewelAbility(
    JewelAbilities.key("life"),
    10.minutes.inWholeMilliseconds
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

        player.world.spawnParticle(Particle.REDSTONE,
            player.location.x, player.location.y, player.location.z,
            155, 1.0, 0.2, 1.0, 2.0,
            Particle.DustOptions(
                Color.fromRGB(255, 0, 0), // red
                1F
            )
        )

        player.world.spawnParticle(Particle.REDSTONE,
            player.location.x, player.location.y + 1, player.location.z,
            60, 0.7, 0.2, 0.7, 2.0,
            Particle.DustOptions(
                Color.fromRGB(255, 185, 56), // gold
                0.6F
            )
        )

        player.world.spawnParticle(Particle.REDSTONE,
            player.location.x, player.location.y + 1.8, player.location.z,
            30, 0.3, 0.2, 0.3, 2.0,
            Particle.DustOptions(
                Color.fromRGB(183, 56, 166), // pink
                0.3F
            )
        )

        return true
    }
}