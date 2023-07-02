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
import xyz.bluspring.onthequest.data.condition.Condition
import xyz.bluspring.onthequest.data.particle.ParticleSpawn
import xyz.bluspring.onthequest.data.util.KeybindType
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

abstract class Ability(val cooldownTicks: Long) {
    // This is stored by (player: triggerTime)
    // The trigger time is when the cooldown has been triggered,
    // which is based off of the server's current tick.
    protected val cooldowns = mutableMapOf<UUID, Long>()
    var keybindType: KeybindType = KeybindType.NONE
    private val isActivated = ConcurrentLinkedDeque<UUID>()

    private val particles = mutableListOf<AbilityParticles>()
    private val conditions = mutableListOf<Condition>()
    private val abilityEvents = mutableMapOf<AbilityEvents, Ability>()

    private val didMeetCondition = mutableListOf<UUID>()

    fun getCooldown(player: Player): Long {
        if (!cooldowns.contains(player.uniqueId))
            return 0L

        val cooldownTime = cooldowns[player.uniqueId]!!
        val endTime = cooldownTime + cooldownTicks
        return (endTime - Bukkit.getServer().currentTick.toLong()).coerceAtLeast(0L)
    }

    fun markActive(player: Player) {
        isActivated.add(player.uniqueId)
    }

    fun isActive(player: Player): Boolean {
        return isActivated.contains(player.uniqueId)
    }

    fun clearActive(player: Player) {
        isActivated.remove(player.uniqueId)
    }

    open fun triggerCooldown(player: Player) {
        cooldowns[player.uniqueId] = Bukkit.getServer().currentTick.toLong()

        Bukkit.getScheduler().runTaskLater(OnTheQuest.plugin, Runnable {
            resetCooldown(player)
        }, cooldownTicks)
    }

    open fun resetCooldown(player: Player) {
        cooldowns.remove(player.uniqueId)
    }

    open fun meetsConditions(player: Player): Boolean {
        return conditions.isEmpty() || conditions.all { it.meetsCondition(player) }
    }

    open fun canTrigger(player: Player): Boolean {
        if (!meetsConditions(player)) {
            if (didMeetCondition.contains(player.uniqueId)) {
                abilityEvents[AbilityEvents.CONDITION_UNFULFILLED]?.trigger(player, null)
                didMeetCondition.removeIf { it == player.uniqueId }
            }

            return false
        }

        if (!cooldowns.contains(player.uniqueId)) {
            if (!didMeetCondition.contains(player.uniqueId))
                didMeetCondition.add(player.uniqueId)

            return true
        }

        if (cooldowns.contains(player.uniqueId) && (Bukkit.getServer().currentTick.toLong() - cooldowns[player.uniqueId]!!) >= cooldownTicks) {
            if (!didMeetCondition.contains(player.uniqueId))
                didMeetCondition.add(player.uniqueId)

            return true
        }

        if (didMeetCondition.contains(player.uniqueId)) {
            abilityEvents[AbilityEvents.CONDITION_UNFULFILLED]?.trigger(player, null)
            didMeetCondition.removeIf { it == player.uniqueId }
        }

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

    open fun resetEffects(player: Player) {}

    open fun triggerParticles(player: Player) {
        val serverPlayer = (player as CraftPlayer).handle
        val level = serverPlayer.level as ServerLevel

        if (particles.isEmpty())
            return

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

                if (json.has("conditions")) {
                    json.getAsJsonArray("conditions").forEach {
                        val data = it.asJsonObject

                        val conditionType = QuestRegistries.CONDITION.get(ResourceLocation.tryParse(data.get("type").asString))
                        val condition = conditionType!!.parse(data.getAsJsonObject("data"))

                        this.conditions.add(condition)
                    }
                }

                if (json.has("ability_events")) {
                    val evs = json.getAsJsonObject("ability_events")
                    evs.keySet().forEach {
                        val eventType = AbilityEvents.valueOf(it.uppercase())

                        val ability = parse(evs.getAsJsonObject(it))
                        this.abilityEvents[eventType] = ability
                    }
                }
            }
        }
    }
}
