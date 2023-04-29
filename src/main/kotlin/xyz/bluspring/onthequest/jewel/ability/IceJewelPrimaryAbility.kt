package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class IceJewelPrimaryAbility : JewelAbility(
    JewelAbilities.key("ice_primary"),
    5.minutes.inWholeMilliseconds
) {
    @EventHandler
    fun onPlayerAttack(ev: EntityDamageByEntityEvent) {
        val damager = ev.damager
        val entity = ev.entity

        if (damager !is Player)
            return

        if (entity !is LivingEntity)
            return

        if (!doesAbilityApply(damager))
            return

        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 5.seconds.ticks, 255))

        entity.world.strikeLightning(entity.location)

        this.run(damager)
    }

    override fun runAbility(player: Player): Boolean {
        player.world.spawnParticle(Particle.SNOWFLAKE, player.location, 155, 0.3, 0.3, 0.3, 0.2)
        player.world.spawnParticle(Particle.DOLPHIN, player.location, 155, 1.0, 1.0, 1.0, 5.0)
        return true
    }
}