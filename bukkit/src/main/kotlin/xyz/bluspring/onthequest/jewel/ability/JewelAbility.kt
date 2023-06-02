package xyz.bluspring.onthequest.jewel.ability

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import xyz.bluspring.onthequest.OnTheQuestBukkit
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import java.lang.Long.max
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

// cooldown is in ticks
abstract class JewelAbility(
    private val id: NamespacedKey,
    val cooldown: Long,
    val timeUntilClear: Long = 0L
) : Listener, Keyed {
    // why is bukkit
    private val cooldowns = mutableMapOf<UUID, Long>()

    init {
        OnTheQuestBukkit.plugin.server.pluginManager.registerEvents(this, OnTheQuestBukkit.plugin)
    }

    override fun getKey(): NamespacedKey {
        return id
    }

    open fun doCooldownCheck(ev: PlayerInteractEvent): Boolean {
        return true
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onJewelCooldownCheck(ev: PlayerInteractEvent) {
        val item = ev.player.inventory.itemInMainHand

        if (JewelEffectEventHandler.getJewelTypes(item)?.any { it.hasAbility(this) } != true)
            return

        if (!doCooldownCheck(ev))
            return

        if (!ev.isCancelled) {
            notifyCooldown(ev.player)
        }
    }

    private fun zeroPad(num: Long): String {
        return if (num <= 9)
            "0$num"
        else
            "$num"
    }

    fun notifyCooldown(player: Player) {
        val cooldownTime = cooldowns[player.uniqueId] ?: return
        val endTime = cooldownTime + cooldown
        val timeUntilEnd = (endTime - System.currentTimeMillis()).coerceAtLeast(0L)

        if (timeUntilEnd >= cooldown)
            return

        val duration = timeUntilEnd.milliseconds

        val str = "${zeroPad(duration.inWholeMinutes - (duration.inWholeHours * 60))}:${
            zeroPad(duration.inWholeSeconds - (duration.inWholeMinutes * 60))}"

        player.sendActionBar(
            Component.translatable("abilities.${id.namespace}.${id.key}")
                .append(Component.text(" is on cooldown: ").color(NamedTextColor.RED))
                .append(Component.text(str).color(NamedTextColor.YELLOW))
        )
    }

    private val timer = Timer("onthequest_ability_timer_$id")

    fun triggerCooldown(player: Player) {
        cooldowns[player.uniqueId] = System.currentTimeMillis()
    }

    fun timeUntilCooldownFinished(player: Player): Long {
        return max(0L, cooldown - (System.currentTimeMillis() - (cooldowns[player.uniqueId] ?: 0)))
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
                    OnTheQuestBukkit.plugin.server.scheduler.runTask(OnTheQuestBukkit.plugin, Runnable {
                        clearAbility(player)
                    })
                }
            }, timeUntilClear)

            return true
        }

        return false
    }

    abstract fun runAbility(player: Player): Boolean
    open fun clearAbility(player: Player) {}
}