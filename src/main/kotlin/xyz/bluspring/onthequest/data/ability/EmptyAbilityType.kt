package xyz.bluspring.onthequest.data.ability

import com.google.gson.JsonObject

class EmptyAbilityType : AbilityType() {
    override fun create(data: JsonObject, cooldownTicks: Long): Ability {
        return EmptyAbility()
    }
}