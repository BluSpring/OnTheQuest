package xyz.bluspring.onthequest.data.ability

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.OnTheQuest
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class TimedAbility(cooldownTicks: Long, val duration: Long) : Ability(cooldownTicks) {
    private val enabledTimes = ConcurrentHashMap<UUID, Long>()

    override fun triggerCooldown(player: Player) {
        if (enabledTimes.contains(player.uniqueId))
            return

        markActive(player)
        enabledTimes[player.uniqueId] = Bukkit.getServer().currentTick.toLong()
        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            super.triggerCooldown(player)
            resetState(player)
            enabledTimes.remove(player.uniqueId)
        }, duration)
    }

    open fun resetState(player: Player) {
        clearActive(player)
    }

    override fun canTrigger(player: Player): Boolean {
        if (enabledTimes.contains(player))
            return true

        return super.canTrigger(player)
    }
}