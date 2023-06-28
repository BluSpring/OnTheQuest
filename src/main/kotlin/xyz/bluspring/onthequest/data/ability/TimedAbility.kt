package xyz.bluspring.onthequest.data.ability

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.OnTheQuest
import java.util.concurrent.ConcurrentHashMap

abstract class TimedAbility(cooldownTicks: Long, val duration: Long) : Ability(cooldownTicks) {
    private val enabledTimes = ConcurrentHashMap<Player, Long>()

    override fun triggerCooldown(player: Player) {
        if (enabledTimes.contains(player))
            return

        enabledTimes[player] = Bukkit.getServer().currentTick.toLong()
        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            super.triggerCooldown(player)
            enabledTimes.remove(player)
        }, duration)
    }

    override fun canTrigger(player: Player): Boolean {
        if (enabledTimes.contains(player))
            return true

        return super.canTrigger(player)
    }
}