package xyz.bluspring.onthequest.data.ability.effect

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.potion.CraftPotionEffectType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityPotionEffectEvent
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType
import xyz.bluspring.onthequest.data.ability.TimedAbility

class EffectDisableAbility(
    cooldownTicks: Long,
    val effects: List<MobEffect>,
    duration: Long
) : TimedAbility(cooldownTicks, duration) {
    override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return event is EntityPotionEffectEvent && super.canTriggerForEvent(player, event)
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        if (event !is EntityPotionEffectEvent)
            return false

        if (event.action != EntityPotionEffectEvent.Action.ADDED)
            return false

        if (event.newEffect == null)
            return false

        if (effects.contains((event.newEffect!!.type as CraftPotionEffectType).handle)) {
            event.isCancelled = true

            return true
        }

        return false
    }

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

            val duration = if (data.has("duration"))
                data.get("duration").asLong
            else
                0L

            return EffectDisableAbility(cooldownTicks, effects, duration).apply {
                abilities.add(this)
            }
        }
    }
}