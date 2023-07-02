package xyz.bluspring.onthequest.data.particle.types

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import xyz.bluspring.onthequest.data.particle.ParticleSpawn

class CircleParticleSpawn : ParticleSpawn<CircleParticleSpawn.CircleSpawnData>() {
    override fun spawnParticles(player: Player, level: ServerLevel, data: CircleSpawnData) {
        val particle = data.particle
        var angle = 0f
        val pos = player.position().add(particle.offset)

        do {
            val x = (data.radius * Mth.sin(angle))
            val z = (data.radius * Mth.cos(angle))

            level.sendParticles(particle.options, pos.x + x, pos.y, pos.z + z, particle.count, particle.delta.x, particle.delta.y, particle.delta.z, particle.speed)

            angle += data.distance
        } while (angle <= 360f)
    }

    override fun createSpawnData(data: JsonObject): CircleSpawnData {
        return CircleSpawnData(data)
    }

    class CircleSpawnData(data: JsonObject) : SpawnData(data) {
        val radius = if (data.has("radius")) data.get("radius").asFloat else 2.5F
        val distance = if (data.has("distance")) data.get("distance").asFloat else 0.1F
    }
}