package xyz.bluspring.onthequest.util

import kotlin.time.Duration

object KotlinHelper {
    public inline val Duration.ticks: Int get() = (this.inWholeMilliseconds / 50).toInt()
}