package xyz.bluspring.onthequest.data.ability.attack

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class AttackPelletApplyAbility(cooldownTicks: Long, val maxDistance: Double, val ability: Ability) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val ray = player.rayTraceBlocks(maxDistance) ?: return true
        if (ray.hitEntity != null) {
            if (ray.hitEntity is Player) {
                val hit = (ray.hitEntity as Player)

                ability.trigger(hit, hit.location)
            }
        }

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val maxDistance = data.get("max_distance").asDouble
            val abilityData = data.getAsJsonObject("ability")
            val ability = if (abilityData.isJsonObject)
                Ability.parse(abilityData.asJsonObject)
            else
                QuestRegistries.ABILITY.get(ResourceLocation.tryParse(abilityData.asString))!!

            return AttackPelletApplyAbility(cooldownTicks, maxDistance, ability)
        }
    }
}