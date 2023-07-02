package xyz.bluspring.onthequest.data.condition.types

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.condition.Condition

class SubmergedInCondition(val fluid: TagKey<Fluid>) : Condition() {
    override fun meetsCondition(player: Player): Boolean {
        val serverPlayer = (player as CraftPlayer).handle

        serverPlayer.isEyeInFluid(fluid)
        TODO("Not yet implemented")
    }

    class Type : Condition.Type() {
        override fun parse(data: JsonObject): Condition {
            return SubmergedInCondition(TagKey.create(Registry.FLUID_REGISTRY, ResourceLocation(data.get("fluid").asString)))
        }
    }
}