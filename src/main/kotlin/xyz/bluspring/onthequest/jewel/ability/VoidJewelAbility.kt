package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.entity.Player
import org.bukkit.entity.WitherSkull
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.time.Duration.Companion.minutes

class VoidJewelAbility : JewelAbility(
    JewelAbilities.key("void"),
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
        // TODO: fire wither skull
        // 30s effect
        player.launchProjectile(WitherSkull::class.java).velocity = player.location.direction.multiply(2)

        return true
    }
}