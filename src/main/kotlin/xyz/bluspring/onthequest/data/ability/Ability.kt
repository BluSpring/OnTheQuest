package xyz.bluspring.onthequest.data.ability

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.particle.ParticleSpawn
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

    private val particles = mutableListOf<AbilityParticles>()

    fun getCooldown(player: Player): Long {
        if (!cooldowns.contains(player))
            return 0L

        return (Bukkit.getServer().currentTick.toLong() - cooldowns[player]!!).coerceAtLeast(0L)
    }

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

    open fun triggerParticles(player: Player) {
        val serverPlayer = (player as CraftPlayer).handle
        val level = serverPlayer.level as ServerLevel

        particles.forEach { particle ->
            val spawn = particle.particleSpawn
            val spawnData = particle.spawnData
            spawn.spawnParticles(serverPlayer, level, spawnData)
        }
    }

    private data class AbilityParticles(
        val particleSpawn: ParticleSpawn<ParticleSpawn.SpawnData>,
        val spawnData: ParticleSpawn.SpawnData
    )

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
            ).apply {
                if (json.has("particles")) {
                    json.getAsJsonArray("particles").forEach {
                        val data = it.asJsonObject

                        val spawnType = QuestRegistries.PARTICLE_SPAWN_TYPE.get(ResourceLocation.tryParse(data.get("type").asString))!!
                        val spawnData = spawnType.createSpawnData(data)
                        this.particles.add(AbilityParticles(spawnType, spawnData))
                    }
                }
            }
        }
    }
}
