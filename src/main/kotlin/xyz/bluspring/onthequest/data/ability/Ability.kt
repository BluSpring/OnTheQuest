package xyz.bluspring.onthequest.data.ability

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

abstract class Ability(val cooldownTicks: Long) {
    // This is stored by (player: triggerTime)
    // The trigger time is when the cooldown has been triggered,
    // which is based off of the server's current tick.
    protected val cooldowns = mutableMapOf<Player, Long>()

    open fun triggerCooldown(player: Player) {
        cooldowns[player] = Bukkit.getServer().currentTick.toLong()
    }

    open fun resetCooldown(player: Player) {
        cooldowns[player] = 0
    }

    open fun canTrigger(player: Player, location: Location): Boolean {
        if (cooldowns.contains(player) && Bukkit.getServer().currentTick.toLong() - cooldowns[player]!! >= cooldownTicks)
            return true

        return false
    }

    open fun runEffects(player: Player, location: Location) {}

    open fun trigger(player: Player, location: Location) {}
    open fun <T : Event> triggerForEvent(player: Player, event: T) {}
}
