package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import java.lang.Long.max
import java.util.Timer
import java.util.TimerTask

// cooldown is in ticks
abstract class JewelAbility(
    val key: NamespacedKey,
    val cooldown: Long
) : Listener, Keyed {
    init {
        OnTheQuest.plugin.server.pluginManager.registerEvents(this, OnTheQuest.plugin)
    }

    override fun getKey(): NamespacedKey {
        return key
    }

    private val timer = Timer("onthequest_ability_timer_$key")

    fun triggerCooldown(player: Player) {
        player.persistentDataContainer.set(key, PersistentDataType.LONG, System.currentTimeMillis())
    }

    fun timeUntilCooldownFinished(player: Player): Long {
        return max(0L, cooldown - (System.currentTimeMillis() - (player.persistentDataContainer.get(key, PersistentDataType.LONG) ?: System.currentTimeMillis())))
    }

    protected fun doesAbilityApply(player: Player): Boolean {
        return (JewelEffectEventHandler.getActiveJewels(player)?.any { it.hasAbility(this) } ?: false) && timeUntilCooldownFinished(player) <= 0
    }

    fun run(player: Player): Boolean {
        if (timeUntilCooldownFinished(player) > 0)
            return false

        if (runAbility(player)) {
            triggerCooldown(player)

            timer.schedule(object : TimerTask() {
                override fun run() {
                    OnTheQuest.plugin.server.scheduler.runTask(OnTheQuest.plugin, Runnable {
                        clearAbility(player)
                    })
                }
            }, cooldown)

            return true
        }

        return false
    }

    abstract fun runAbility(player: Player): Boolean
    open fun clearAbility(player: Player) {}
}