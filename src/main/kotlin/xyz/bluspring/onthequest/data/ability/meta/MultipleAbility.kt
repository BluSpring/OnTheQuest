package xyz.bluspring.onthequest.data.ability.meta

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class MultipleAbility(cooldownTicks: Long, val abilities: List<Ability>) : Ability(cooldownTicks) {
    override fun canTrigger(player: Player): Boolean {
        return super.canTrigger(player) && abilities.all { it.canTrigger(player) }
    }

    override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return super.canTriggerForEvent(player, event) && abilities.all { it.canTriggerForEvent(player, event) }
    }

    override fun trigger(player: Player, location: Location?): Boolean {
        return abilities.all { it.trigger(player, location) }
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        return abilities.all { it.triggerForEvent(player, event) }
    }

    override fun resetEffects(player: Player) {
        abilities.forEach { it.resetEffects(player) }
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val abilities = mutableListOf<Ability>()

            data.getAsJsonArray("abilities").forEach { abilityData ->
                if (abilityData.isJsonObject) {
                    val json = abilityData.asJsonObject

                    abilities.add(Ability.parse(json))
                } else {
                    val resourcePath = ResourceLocation.tryParse(abilityData.asString)!!
                    abilities.add(QuestRegistries.ABILITY.get(resourcePath)!!)
                }
            }

            return MultipleAbility(cooldownTicks, abilities)
        }
    }
}