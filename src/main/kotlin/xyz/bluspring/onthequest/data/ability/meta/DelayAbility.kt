package xyz.bluspring.onthequest.data.ability.meta

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class DelayAbility(cooldownTicks: Long, val delay: Long, val ability: Ability) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            ability.trigger(player, location)
        }, delay)
        return true
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            ability.triggerForEvent(player, event)
        }, delay)
        return true
    }

    override fun resetEffects(player: Player) {
        ability.resetEffects(player)
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val abilityData = data.getAsJsonObject("ability")

            val ability = if (abilityData.isJsonObject) {
                val json = abilityData.asJsonObject

                Ability.parse(json)
            } else {
                val resourcePath = ResourceLocation.tryParse(abilityData.asString)!!
                QuestRegistries.ABILITY.get(resourcePath)!!
            }

            val delay = data.get("delay").asLong

            return DelayAbility(cooldownTicks, delay, ability)
        }
    }
}