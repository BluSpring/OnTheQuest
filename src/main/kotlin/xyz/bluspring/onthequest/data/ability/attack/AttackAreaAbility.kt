package xyz.bluspring.onthequest.data.ability.attack

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class AttackAreaAbility(cooldownTicks: Long, val ignoreSelf: Boolean, val radius: Double, val ability: Ability) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val entities = player.world.getNearbyLivingEntities(player.location, radius)

        entities.forEach {
            if (it !is Player)
                return@forEach

            if (ignoreSelf && it == player)
                return@forEach

            ability.trigger(it, it.location)
        }

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val radius = data.get("max_radius").asDouble

            val ignoreSelf = if (data.has("ignore_self"))
                data.get("ignore_self").asBoolean
            else true

            val abilityData = data.getAsJsonObject("ability")
            val ability = if (abilityData.isJsonObject)
                Ability.parse(abilityData.asJsonObject)
            else
                QuestRegistries.ABILITY.get(ResourceLocation.tryParse(abilityData.asString))!!

            return AttackAreaAbility(cooldownTicks, ignoreSelf, radius, ability)
        }
    }
}