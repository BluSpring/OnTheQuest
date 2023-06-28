package xyz.bluspring.onthequest.data.particle

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player

abstract class ParticleSpawn<in T : ParticleSpawn.SpawnData> {
    abstract fun spawnParticles(player: Player, level: ServerLevel, data: T)
    abstract fun createSpawnData(data: JsonObject): SpawnData

    open class SpawnData(data: JsonObject) {
        val particle: ParticleData = ParticleData.parse(data)
    }
}