package xyz.bluspring.onthequest.data.particle.types

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import xyz.bluspring.onthequest.data.particle.ParticleSpawn

class RayParticleSpawn : ParticleSpawn<RayParticleSpawn.RaySpawnData>() {
    override fun spawnParticles(player: Player, level: ServerLevel, data: RaySpawnData) {
        val particle = data.particle
        val start = player.eyePosition
        val ray = player.getRayTrace(data.maxDistance).location

        val vec3d2 = ray.subtract(start)
        val normalized = vec3d2.normalize()

        var current = 1.0
        val max = Mth.floor(vec3d2.length()) + 7

        do {
            val currentPos = start.add(normalized.multiply(current, current, current))
            val offset = currentPos.add(particle.offset)

            level.sendParticles(player as ServerPlayer, particle.options, true, offset.x, offset.y, offset.z, particle.count, particle.delta.x, particle.delta.y, particle.delta.z, particle.speed)

            current += data.increment
        } while (current <= max)
    }

    override fun createSpawnData(data: JsonObject): RaySpawnData {
        return RaySpawnData(data)
    }

    class RaySpawnData(data: JsonObject) : SpawnData(data) {
        val maxDistance = data.get("max_distance").asInt
        val increment = data.get("increment").asDouble
    }
}