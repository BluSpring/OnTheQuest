package xyz.bluspring.onthequest.data.ability.spawn

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class SpawnProjectileAbility(cooldownTicks: Long, val entityType: EntityType<out Projectile>, val speed: Float, val divergence: Float) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val nmsPlayer = (player as CraftPlayer).handle

        val entity = entityType.create(nmsPlayer.level) ?: return false

        val pos = nmsPlayer.eyePosition.add(Vec3(1.0, 1.0, 1.0).multiply(nmsPlayer.lookAngle))
        entity.absMoveTo(pos.x, pos.y, pos.z)
        nmsPlayer.level.addFreshEntity(entity)
        entity.shoot(nmsPlayer.lookAngle.x, nmsPlayer.lookAngle.y, nmsPlayer.lookAngle.z, speed, divergence)

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val entityType = Registry.ENTITY_TYPE.get(ResourceLocation.tryParse(data.get("entity").asString)!!)
            val speed = if (data.has("speed")) data.get("speed").asFloat else 1f
            val divergence = if (data.has("divergence")) data.get("divergence").asFloat else 0f

            return SpawnProjectileAbility(cooldownTicks, entityType as EntityType<out Projectile>, speed, divergence)
        }
    }
}