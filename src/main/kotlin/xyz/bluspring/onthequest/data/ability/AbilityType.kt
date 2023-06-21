package xyz.bluspring.onthequest.data.ability

import com.google.gson.JsonObject

abstract class AbilityType {
    abstract fun create(data: JsonObject): Ability
}