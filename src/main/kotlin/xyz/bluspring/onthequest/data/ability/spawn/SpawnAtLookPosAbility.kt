package xyz.bluspring.onthequest.data.ability.spawn

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class SpawnAtLookPosAbility(cooldownTicks: Long, val entityType: EntityType<*>) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val lookPos = player.rayTraceBlocks(150.0) ?: return false

        val level = (player.world as CraftWorld).handle
        val entity = entityType.create(level) ?: return false

        entity.absMoveTo(lookPos.hitPosition.x, lookPos.hitPosition.y, lookPos.hitPosition.z)
        level.addFreshEntity(entity)

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val entityType = ResourceLocation.tryParse(data.get("entity").asString)!!

            return SpawnAtLookPosAbility(cooldownTicks, Registry.ENTITY_TYPE.get(entityType))
        }
    }
}