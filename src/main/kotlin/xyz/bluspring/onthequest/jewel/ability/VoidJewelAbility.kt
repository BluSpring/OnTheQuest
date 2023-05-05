package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.WitherSkull
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.Plugin
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import kotlin.time.Duration.Companion.minutes

class VoidJewelAbility : JewelAbility(
    JewelAbilities.key("void"),
    3.minutes.inWholeMilliseconds
) {
    @EventHandler
    fun onPlayerRightClick(ev: PlayerInteractEvent) {
        if (ev.item != null)
            return

        if (!doesAbilityApply(ev.player) || JewelEffectEventHandler.getJewelTypes(ev.item!!)?.any { it.hasAbility(this) } != true)
            return

        if (this.run(ev.player)) {
            ev.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDamagedBySkull(ev: EntityDamageByEntityEvent) {
        if (ev.damager !is WitherSkull)
            return

        if (ev.damager.hasMetadata("otq_isPlayerFired")) {
            ev.damage += 3
        }
    }

    override fun runAbility(player: Player): Boolean {
        val skull = player.launchProjectile(WitherSkull::class.java)
        skull.setMetadata("otq_isPlayerFired", object : MetadataValue {
            override fun value(): Any {
                return true
            }

            override fun asInt(): Int {
                TODO("Not yet implemented")
            }

            override fun asFloat(): Float {
                TODO("Not yet implemented")
            }

            override fun asDouble(): Double {
                TODO("Not yet implemented")
            }

            override fun asLong(): Long {
                TODO("Not yet implemented")
            }

            override fun asShort(): Short {
                TODO("Not yet implemented")
            }

            override fun asByte(): Byte {
                TODO("Not yet implemented")
            }

            override fun asBoolean(): Boolean {
                return true
            }

            override fun asString(): String {
                TODO("Not yet implemented")
            }

            override fun getOwningPlugin(): Plugin {
                return OnTheQuest.plugin
            }

            override fun invalidate() {
                TODO("Not yet implemented")
            }

        })
        skull.velocity = player.location.direction.multiply(2)

        player.world.spawnParticle(
            Particle.REDSTONE,
            player.location.x, player.location.y, player.location.z,
            155, 0.4, 1.4, 0.4, 15.0,
            Particle.DustOptions(
                Color.fromRGB(51, 51, 51), // pink
                1F
            )
        )

        player.world.spawnParticle(Particle.WARPED_SPORE, player.location, 50, 0.6, 1.0, 0.6, 0.4)

        return true
    }
}