package xyz.bluspring.onthequest.data.particle.types

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import xyz.bluspring.onthequest.data.particle.ParticleSpawn

class SimpleParticleSpawn : ParticleSpawn<SimpleParticleSpawn.SimpleSpawnData>() {
    override fun spawnParticles(player: Player, level: ServerLevel, data: SimpleSpawnData) {
        val particle = data.particle
        val pos = player.position().add(particle.offset)
        level.sendParticles(particle.options, pos.x, pos.y, pos.z, particle.count, particle.delta.x, particle.delta.y, particle.delta.z, particle.speed)
    }

    override fun createSpawnData(data: JsonObject): SimpleSpawnData {
        return SimpleSpawnData(data)
    }

    class SimpleSpawnData(data: JsonObject) : SpawnData(data) {
    }
}