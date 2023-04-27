package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SkeletalJewelAbility : JewelAbility(
    JewelAbilities.key("skeletal"),
    20.minutes.inWholeMilliseconds
) {
    @EventHandler
    fun onPlayerRightClick(ev: PlayerInteractEvent) {
        if (!doesAbilityApply(ev.player))
            return

        if (this.run(ev.player)) {
            ev.isCancelled = true

            if (ev.item != null) {
                ev.player.setCooldown(ev.item!!.type, (this.cooldown / 50).toInt())
            }
        }
    }

    override fun runAbility(player: Player): Boolean {
        val entities = player.world.getNearbyLivingEntities(player.location, 100.0)
        entities.forEach {
            if (it !is Player)
                return@forEach

            if (it.uniqueId == player.uniqueId)
                return@forEach

            it.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 5.minutes.ticks, 0))

            if (it.location.distance(player.location) <= 32) {
                it.addPotionEffect(PotionEffect(PotionEffectType.POISON, 10.seconds.ticks, 0))
            }
        }

        return true
    }
}