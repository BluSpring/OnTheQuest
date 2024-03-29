package xyz.bluspring.onthequest.data.ability.meta

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType
import java.util.*

class AmmunitionAbility(cooldownTicks: Long, val maxAmmo: Int, val ability: Ability) : Ability(cooldownTicks) {
    private val ammunition = mutableMapOf<UUID, Int>()

    override fun canTrigger(player: Player): Boolean {
        val canTrigger = super.canTrigger(player)
        if (!ammunition.contains(player.uniqueId) && canTrigger)
            return true

        return ammunition.contains(player.uniqueId) && ammunition[player.uniqueId]!! > 0
    }

    override fun resetCooldown(player: Player) {
        super.resetCooldown(player)
        ammunition.remove(player.uniqueId)
    }

    override fun trigger(player: Player, location: Location?): Boolean {
        val currentAmmo = if (!ammunition.contains(player.uniqueId))
            maxAmmo
        else
            ammunition[player.uniqueId]!!

        if (ability.trigger(player, location) && getCooldown(player) <= 0) {
            ability.triggerCooldown(player)
        }

        ammunition[player.uniqueId] = currentAmmo - 1

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val maxAmmo = data.get("max_ammo").asInt
            val abilityData = data.get("ability")
            val ability = if (abilityData.isJsonObject)
                Ability.parse(abilityData.asJsonObject)
            else
                QuestRegistries.ABILITY.get(ResourceLocation.tryParse(abilityData.asString))!!

            return AmmunitionAbility(cooldownTicks, maxAmmo, ability)
        }
    }
}