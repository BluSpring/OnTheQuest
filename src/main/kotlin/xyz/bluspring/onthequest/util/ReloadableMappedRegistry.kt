package xyz.bluspring.onthequest.util

import com.mojang.serialization.Lifecycle
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

class ReloadableMappedRegistry<T : Any>(key: ResourceKey<out Registry<T>>, lifecycle: Lifecycle) : MappedRegistry<T>(key, lifecycle, null) {
    override fun register(resourceKey: ResourceKey<T>, obj: T, lifecycle: Lifecycle): Holder<T> {
        if (this.containsKey(resourceKey)) {
            val original = super.get(resourceKey)
            val id = super.getId(original)

            return super.registerMapping(id, resourceKey, obj, lifecycle)
        }

        return super.register(resourceKey, obj, lifecycle)
    }
}
