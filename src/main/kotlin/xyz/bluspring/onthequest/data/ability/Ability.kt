package xyz.bluspring.onthequest.data.ability

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.util.KeybindType
import java.util.concurrent.ConcurrentHashMap

abstract class Ability(val cooldownTicks: Long) {
    // This is stored by (player: triggerTime)
    // The trigger time is when the cooldown has been triggered,
    // which is based off of the server's current tick.
    protected val cooldowns = ConcurrentHashMap<Player, Long>()
    var keybindType: KeybindType = KeybindType.NONE

    open fun triggerCooldown(player: Player) {
        cooldowns[player] = Bukkit.getServer().currentTick.toLong()

        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            resetCooldown(player)
        }, cooldownTicks)
    }

    open fun resetCooldown(player: Player) {
        cooldowns.remove(player)
    }

    open fun canTrigger(player: Player): Boolean {
        if (!cooldowns.contains(player))
            return true

        if (cooldowns.contains(player) && Bukkit.getServer().currentTick.toLong() - cooldowns[player]!! >= cooldownTicks)
            return true

        return false
    }

    open fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return canTrigger(player)
    }

    open fun trigger(player: Player, location: Location?): Boolean {
        return false
    }

    open fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        return false
    }
}
