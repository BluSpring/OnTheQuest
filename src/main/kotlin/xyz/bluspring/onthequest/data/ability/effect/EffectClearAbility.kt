package xyz.bluspring.onthequest.data.ability.effect

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class EffectClearAbility(
    cooldownTicks: Long,
    val effects: List<MobEffect>
) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location): Boolean {
        if (effects.isEmpty()) {
            (player as CraftPlayer).handle.removeAllEffects()
            return true
        }

        effects.forEach {
            (player as CraftPlayer).handle.removeEffect(it)
        }

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val effects = mutableListOf<MobEffect>()

            if (data.has("effects"))
                data.getAsJsonArray("effects").forEach {
                    effects.add(Registry.MOB_EFFECT.get(ResourceLocation.tryParse(it.asString))!!)
                }

            return EffectClearAbility(cooldownTicks, effects).apply {
                abilities.add(this)
            }
        }
    }
}