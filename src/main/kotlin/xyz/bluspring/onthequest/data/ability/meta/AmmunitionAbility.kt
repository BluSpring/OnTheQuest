package xyz.bluspring.onthequest.data.ability.meta

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Location
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType
import java.util.concurrent.ConcurrentHashMap

class AmmunitionAbility(cooldownTicks: Long, val maxAmmo: Int, val ability: Ability) : Ability(cooldownTicks) {
    private val ammunition = ConcurrentHashMap<Player, Int>()

    override fun canTrigger(player: Player): Boolean {
        val canTrigger = super.canTrigger(player)
        if (!ammunition.contains(player) && canTrigger)
            return true

        return ammunition.contains(player) && ammunition[player]!! > 0
    }

    override fun resetCooldown(player: Player) {
        super.resetCooldown(player)
        ammunition.remove(player)
    }

    override fun trigger(player: Player, location: Location?): Boolean {
        val currentAmmo = if (!ammunition.contains(player))
            maxAmmo
        else
            ammunition[player]!!

        if (ability.trigger(player, location)) {
            ability.triggerCooldown(player)
        }

        ammunition[player] = currentAmmo - 1

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