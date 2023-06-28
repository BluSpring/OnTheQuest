package xyz.bluspring.onthequest.data.particle.types

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import xyz.bluspring.onthequest.data.particle.ParticleSpawn

class EmptyParticleSpawn : ParticleSpawn<ParticleSpawn.SpawnData>() {
    override fun spawnParticles(player: Player, level: ServerLevel, data: SpawnData) {
    }

    override fun createSpawnData(data: JsonObject): SpawnData {
        return SpawnData(data)
    }
}