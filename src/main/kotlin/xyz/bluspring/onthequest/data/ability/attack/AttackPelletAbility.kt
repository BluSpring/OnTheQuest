package xyz.bluspring.onthequest.data.ability.attack

import com.google.gson.JsonObject
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class AttackPelletAbility(cooldownTicks: Long, val maxDistance: Double, val damageAmount: Double) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val ray = player.rayTraceBlocks(maxDistance) ?: return true
        if (ray.hitEntity != null) {
            if (ray.hitEntity is LivingEntity) {
                (ray.hitEntity as LivingEntity).damage(damageAmount, player)
            }
        }

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val maxDistance = data.get("max_distance").asDouble
            val damageAmount = data.get("damage").asDouble

            return AttackPelletAbility(cooldownTicks, maxDistance, damageAmount)
        }
    }
}