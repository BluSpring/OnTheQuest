package xyz.bluspring.onthequest.data.ability

import com.google.gson.JsonObject

abstract class AbilityType {
    val abilities = mutableListOf<Ability>()

    open fun all(): List<Ability> {
        return abilities
    }

    open fun clear() {
        abilities.clear()
    }

    abstract fun create(data: JsonObject, cooldownTicks: Long): Ability
}