package xyz.bluspring.onthequest.data.particle

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

data class ParticleData(
    val options: ParticleOptions,
    val offset: Vec3,
    val count: Int,
    val delta: Vec3,
    val speed: Double
) {
    companion object {
        fun parse(data: JsonObject): ParticleData {
            val particleTypeId = ResourceLocation.tryParse(data.get("particle_type").asString)
            val particleType = Registry.PARTICLE_TYPE.get(particleTypeId)!!

            if (particleType !is SimpleParticleType && !data.has("particle_data"))
                throw IllegalStateException("Particle type $particleTypeId requires the particle_data tag to be present!")

            val options = if (particleType !is SimpleParticleType) {
                particleType.codec().decode(JsonOps.INSTANCE, data.get("particle_data").asJsonObject).result().get().first
            } else
                particleType

            val offsetX = if (data.has("x")) data.get("x").asDouble else 0.0
            val offsetY = if (data.has("y")) data.get("y").asDouble else 0.0
            val offsetZ = if (data.has("z")) data.get("z").asDouble else 0.0

            val deltaX = if (data.has("dx")) data.get("dx").asDouble else 0.0
            val deltaY = if (data.has("dx")) data.get("dy").asDouble else 0.0
            val deltaZ = if (data.has("dx")) data.get("dz").asDouble else 0.0

            val speed = if (data.has("speed")) data.get("speed").asDouble else 1.0
            val count = if (data.has("count")) data.get("count").asInt else 1

            return ParticleData(options, Vec3(offsetX, offsetY, offsetZ), count, Vec3(deltaX, deltaY, deltaZ), speed)
        }
    }
}
