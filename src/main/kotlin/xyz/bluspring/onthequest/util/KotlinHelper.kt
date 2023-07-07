package xyz.bluspring.onthequest.util

import org.bukkit.Bukkit
import xyz.bluspring.onthequest.OnTheQuest
import kotlin.time.Duration

object KotlinHelper {
    public inline val Duration.ticks: Int get() = (this.inWholeMilliseconds / 50).toInt()
    fun delayByOneTick(runnable: Runnable) {
        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, runnable, 1L)
    }
}