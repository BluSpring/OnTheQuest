package xyz.bluspring.onthequest.data.condition

import com.google.gson.JsonObject
import org.bukkit.entity.Player

class EmptyCondition : Condition() {
    override fun meetsCondition(player: Player): Boolean {
        return true
    }

    class Type : Condition.Type() {
        override fun parse(data: JsonObject): Condition {
            return EmptyCondition()
        }
    }
}