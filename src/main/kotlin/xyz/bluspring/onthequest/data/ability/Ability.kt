package xyz.bluspring.onthequest.data.ability

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.util.KeybindType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

abstract class Ability(val cooldownTicks: Long) {
    // This is stored by (player: triggerTime)
    // The trigger time is when the cooldown has been triggered,
    // which is based off of the server's current tick.
    protected val cooldowns = ConcurrentHashMap<Player, Long>()
    var keybindType: KeybindType = KeybindType.NONE
    private val isActivated = ConcurrentLinkedDeque<Player>()

    fun markActive(player: Player) {
        isActivated.add(player)
    }

    fun isActive(player: Player): Boolean {
        return isActivated.contains(player)
    }

    fun clearActive(player: Player) {
        isActivated.remove(player)
    }

    open fun triggerCooldown(player: Player) {
        cooldowns[player] = Bukkit.getServer().currentTick.toLong()

        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            resetCooldown(player)
        }, cooldownTicks)
    }

    open fun resetCooldown(player: Player) {
        cooldowns.remove(player)
    }

    open fun canTrigger(player: Player): Boolean {
        if (!cooldowns.contains(player))
            return true

        if (cooldowns.contains(player) && Bukkit.getServer().currentTick.toLong() - cooldowns[player]!! >= cooldownTicks)
            return true

        return false
    }

    open fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return (keybindType.isNone() || isActive(player)) && canTrigger(player)
    }

    open fun trigger(player: Player, location: Location?): Boolean {
        return false
    }

    open fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        return false
    }

    companion object {
        fun parse(json: JsonObject): Ability {
            val abilityType = QuestRegistries.ABILITY_TYPE.get(ResourceLocation.tryParse(json.get("type").asString))!!

            return abilityType.create(
                if (json.has("data"))
                    json.getAsJsonObject("data")
                else
                    JsonObject(),
                if (json.has("cooldown"))
                    json.get("cooldown").asLong
                else
                    0L
            )
        }
    }
}
