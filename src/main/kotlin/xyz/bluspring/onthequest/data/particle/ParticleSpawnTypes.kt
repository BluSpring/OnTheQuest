package xyz.bluspring.onthequest.data.particle

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.particle.types.CircleParticleSpawn
import xyz.bluspring.onthequest.data.particle.types.EmptyParticleSpawn
import xyz.bluspring.onthequest.data.particle.types.RayParticleSpawn
import xyz.bluspring.onthequest.data.particle.types.SimpleParticleSpawn

object ParticleSpawnTypes {
    val EMPTY = register("empty", EmptyParticleSpawn())

    val SIMPLE = register("simple", SimpleParticleSpawn())
    val RAY = register("ray", RayParticleSpawn())
    val CIRCLE = register("circle", CircleParticleSpawn())

    fun init() {}

    private fun register(key: String, type: ParticleSpawn<*>): ParticleSpawn<ParticleSpawn.SpawnData> {
        return Registry.register(QuestRegistries.PARTICLE_SPAWN_TYPE, ResourceLocation("questsmp", key), type as ParticleSpawn<ParticleSpawn.SpawnData>)
    }
}