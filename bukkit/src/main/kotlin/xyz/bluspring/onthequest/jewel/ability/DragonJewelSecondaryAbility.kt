package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Material
import org.bukkit.entity.DragonFireball
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import kotlin.time.Duration.Companion.minutes

class DragonJewelSecondaryAbility : JewelAbility(
    JewelAbilities.key("dragon_secondary"),
    10.minutes.inWholeMilliseconds
) {
    override fun doCooldownCheck(ev: PlayerInteractEvent): Boolean {
        return ev.action.isRightClick
    }

    @EventHandler
    fun onPlayerRightClick(ev: PlayerInteractEvent) {
        val item = ev.player.inventory.itemInMainHand

        if (!doesAbilityApply(ev.player) || item.type != Material.EMERALD || JewelEffectEventHandler.getJewelTypes(item)?.any { it.hasAbility(this) } != true)
            return

        if (!ev.action.isRightClick)
            return

        if (this.run(ev.player)) {
            ev.isCancelled = true
        }
    }

    override fun runAbility(player: Player): Boolean {
        val dragonBall = player.launchProjectile(DragonFireball::class.java)
        dragonBall.velocity = player.location.direction.multiply(2)

        return true
    }
}