package xyz.bluspring.onthequest.data.ability.effect

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffectInstance
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class EffectAddAbility(
    cooldownTicks: Long,
    val effects: List<MobEffectInstance>
) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        val nmsPlayer = (player as CraftPlayer).handle
        effects.forEach {
            if (nmsPlayer.hasEffect(it.effect))
                return@forEach

            nmsPlayer.addEffect(it)
        }

        return true
    }

    override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return (event is PlayerJoinEvent || event is PlayerMoveEvent || event is PlayerRespawnEvent) && super.canTriggerForEvent(player, event)
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        return trigger(player, null)
    }

    override fun resetEffects(player: Player) {
        effects.forEach {
            (player as CraftPlayer).handle.removeEffect(it.effect)
        }
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val effects = mutableListOf<MobEffectInstance>()

            data.getAsJsonArray("effects").forEach {
                val effectData = it.asJsonObject

                val effect = Registry.MOB_EFFECT.get(ResourceLocation.tryParse(effectData.get("id").asString))
                val duration = effectData.get("duration").asInt
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