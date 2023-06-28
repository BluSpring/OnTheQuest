package xyz.bluspring.onthequest.data.ability.attack

import com.google.gson.JsonObject
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class AttackApplyAbility(cooldownTicks: Long, val ability: Ability) : Ability(cooldownTicks) {
    override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return event is EntityDamageByEntityEvent && super.canTriggerForEvent(player, event)
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        if (event !is EntityDamageByEntityEvent)
            return false

        if (event.entity !is Player)
            return false

        ability.trigger(event.entity as Player, event.entity.location)
        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val ability = Ability.parse(data.asJsonObject)

            return AttackApplyAbility(cooldownTicks, ability)
        }
    }
}