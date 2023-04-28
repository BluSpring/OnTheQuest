package xyz.bluspring.onthequest.jewel.ability

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import java.lang.Long.max
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

// cooldown is in ticks
abstract class JewelAbility(
    private val id: NamespacedKey,
    val cooldown: Long
) : Listener, Keyed {
    init {
        OnTheQuest.plugin.server.pluginManager.registerEvents(this, OnTheQuest.plugin)
    }

    override fun getKey(): NamespacedKey {
        return id
    }

    @EventHandler
    fun onJewelCooldownCheck(ev: PlayerInteractEvent) {
        if (!hasAbilityJewel(ev.player))
            return

        if (ev.action.isRightClick) {
            notifyCooldown(ev.player)
        }
    }

    fun notifyCooldown(player: Player) {
        val cooldownTime = player.persistentDataContainer.get(id, PersistentDataType.LONG) ?: return
        val timeUntilEnd = System.currentTimeMillis() - cooldownTime

        if (timeUntilEnd >= cooldown)
            return

        val duration = timeUntilEnd.milliseconds

        val str = "${duration.inWholeMinutes - duration.inWholeHours.milliseconds.inWholeMinutes}:${
            duration.inWholeSeconds - duration.inWholeMinutes.milliseconds.inWholeSeconds}"

        player.sendActionBar(
            Component.translatable("item.${id.namespace}.${id.key}")
                .append(Component.text(" is on cooldown: ").color(NamedTextColor.RED))
                .append(Component.text(str).color(NamedTextColor.YELLOW))
        )
    }

    private val timer = Timer("onthequest_ability_timer_$id")

    fun triggerCooldown(player: Player) {
        player.persistentDataContainer.set(id, PersistentDataType.LONG, System.currentTimeMillis())
    }

    fun timeUntilCooldownFinished(player: Player): Long {
        return max(0L, cooldown - (System.currentTimeMillis() - (player.persistentDataContainer.get(id, PersistentDataType.LONG) ?: 0)))
    }

    protected fun hasAbilityJewel(player: Player): Boolean {
        val activeJewels = JewelEffectEventHandler.getActiveJewels(player)

        return (activeJewels?.any { it.hasAbility(this) } ?: false)
    }

    protected fun doesAbilityApply(player: Player): Boolean {
        val timeUntilCooldown = timeUntilCooldownFinished(player)
        return hasAbilityJewel(player) && timeUntilCooldown <= 0
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