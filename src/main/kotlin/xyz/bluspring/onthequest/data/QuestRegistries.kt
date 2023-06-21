package xyz.bluspring.onthequest.data

import com.mojang.serialization.Lifecycle
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.ability.AbilityType
import xyz.bluspring.onthequest.data.ability.AbilityTypes
import java.util.function.Supplier

object QuestRegistries {
    private val loaders = mutableMapOf<ResourceLocation, Supplier<*>>()

    fun init() {}

    val ABILITY_TYPE_REGISTRY: ResourceKey<Registry<AbilityType>> = createRegistryKey("ability_type")
    val ABILITY_TYPE = registerSimple(ABILITY_TYPE_REGISTRY) {
        AbilityTypes.EMPTY
    }

    private fun <T : Any> registerSimple(key: ResourceKey<out Registry<T>>, defaultEntryGetter: RegistryBootstrap<T>): Registry<T> {
        val location = key.location()
        val registry = MappedRegistry(key, Lifecycle.experimental(), null)

        loaders[location] = Supplier {
            defaultEntryGetter.run(registry)
        }

        (Registry.REGISTRY as WritableRegistry<WritableRegistry<*>>)
            .register(key as ResourceKey<WritableRegistry<*>>, registry, Lifecycle.experimental())

        return registry
    }

    private fun <T> createRegistryKey(key: String): ResourceKey<Registry<T>> {
        return ResourceKey.createRegistryKey(ResourceLocation("questsmp", key))
    }

    fun interface RegistryBootstrap<T> {
        fun run(registry: Registry<T>): T
    }

    init {
        loaders.forEach { (id, defaultEntry) ->
            if (defaultEntry.get() == null) {
                OnTheQuest.plugin.slF4JLogger.error("Failed to bootstrap registry $id")
            }
        }
    }
}