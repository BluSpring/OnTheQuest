package xyz.bluspring.onthequest.data.condition

import com.google.gson.JsonObject
import org.bukkit.entity.Player

abstract class Condition {
    abstract fun meetsCondition(player: Player): Boolean

    abstract class Type {
        abstract fun parse(data: JsonObject): Condition
    }
}