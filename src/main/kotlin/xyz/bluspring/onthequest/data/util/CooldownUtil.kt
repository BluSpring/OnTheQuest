package xyz.bluspring.onthequest.data.util

import kotlin.time.Duration.Companion.milliseconds

object CooldownUtil {
    fun getSubscriptOfNumber(num: Int): String {
        var text = ""

        num.toString().forEach {
            text += Char(8320 + it.digitToInt())
        }

        return text
    }

    fun getTimeString(ms: Long): String {
        val duration = ms.milliseconds
        return "${zeroPad(duration.inWholeMinutes - (duration.inWholeHours * 60))}:${
            zeroPad(duration.inWholeSeconds - (duration.inWholeMinutes * 60))}"
    }

    private fun zeroPad(num: Long): String {
        return if (num <= 9)
            "0$num"
        else
            "$num"
    }
}