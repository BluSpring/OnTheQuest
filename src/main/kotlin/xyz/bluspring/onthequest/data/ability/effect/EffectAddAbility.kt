package xyz.bluspring.onthequest.data.ability.effect

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffectInstance
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class EffectAddAbility(
    cooldownTicks: Long,
    val effects: List<MobEffectInstance>
) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        effects.forEach {
            (player as CraftPlayer).handle.addEffect(it)
        }

        return true
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        return trigger(player, null)
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val effects = mutableListOf<MobEffectInstance>()

            data.getAsJsonArray("effects").forEach {
                val effectData = it.asJsonObject

                val effect = Registry.MOB_EFFECT.get(ResourceLocation.tryParse(effectData.get("id").asString))
                val duration = effectData.get("duration_tick").asInt
                val amplifier = effectData.get("amplifier").asInt
                val ambient = if (effectData.has("ambient")) effectData.get("ambient").asBoolean else false
                val showParticles = if (effectData.has("particles")) effectData.get("particles").asBoolean else true
                val showIcon = if (effectData.has("show_icon")) effectData.get("show_icon").asBoolean else true

                effects.add(MobEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon))
            }

            return EffectAddAbility(cooldownTicks, effects)
        }
    }
}