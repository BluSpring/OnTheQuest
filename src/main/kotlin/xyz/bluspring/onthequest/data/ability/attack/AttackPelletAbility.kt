package xyz.bluspring.onthequest.data.ability.attack

import com.google.gson.JsonObject
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class AttackPelletAbility(cooldownTicks: Long, val maxDistance: Double, val damageAmount: Double) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val nmsPlayer = (player as CraftPlayer).handle

        val rayTrace = nmsPlayer.getTargetEntity(maxDistance.toInt()) ?: return true

        if (rayTrace.type == HitResult.Type.ENTITY) {
            (rayTrace as EntityHitResult).entity.hurt(DamageSource.playerAttack(nmsPlayer), damageAmount.toFloat())
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