package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class IceJewelSecondaryAbility : JewelAbility(
    JewelAbilities.key("ice_secondary"),
    30.minutes.inWholeMilliseconds
) {
    private val applies = mutableListOf<Player>()

    @EventHandler
    fun onPlayerDamage(ev: EntityDamageEvent) {
        val entity = ev.entity

        if (entity !is Player)
            return

        if (!doesAbilityApply(entity))
            return

        if (entity.health - ev.finalDamage <= 1) {
            runAbility(entity)

            if (!applies.contains(entity)) {
                applies.add(entity)

                OnTheQuest.plugin.server.scheduler.runTaskLater(OnTheQuest.plugin, Runnable {
                    this.run(entity)
                    applies.remove(entity)
                }, 5.seconds.ticks.toLong())
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(ev: PlayerDeathEvent) {
        if (!doesAbilityApply(ev.player))
            return

        ev.isCancelled = true
        runAbility(ev.player)

        if (!applies.contains(ev.player)) {
            applies.add(ev.player)

            OnTheQuest.plugin.server.scheduler.runTaskLater(OnTheQuest.plugin, Runnable {
                this.run(ev.player)
                applies.remove(ev.player)
            }, 5.seconds.ticks.toLong())
        }
    }

    override fun runAbility(player: Player): Boolean {
        player.health = player.health.coerceAtLeast(1.0)
        player.world.spawnParticle(Particle.SNOWFLAKE, player.location, 155, 0.3, 0.3, 0.3, 0.2)
        player.world.spawnParticle(Particle.DOLPHIN, player.location, 155, 1.0, 1.0, 1.0, 5.0)

        return true
    }
}