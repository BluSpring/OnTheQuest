package xyz.bluspring.onthequest.data.util

import com.mojang.serialization.Lifecycle
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import java.util.function.Function

// A registry type that is fully capable of having the same registry keys be re-registered without
// much work needing to be done to handle it.
class ReloadableMappedRegistry<T : Any>(key: ResourceKey<out Registry<T>>, lifecycle: Lifecycle, valueToEntry: Function<T, Holder.Reference<T>>?) : MappedRegistry<T>(key, lifecycle, valueToEntry) {
    constructor(key: ResourceKey<out Registry<T>>, lifecycle: Lifecycle) : this(key, lifecycle, null)

    override fun register(resourceKey: ResourceKey<T>, obj: T, lifecycle: Lifecycle): Holder<T> {
        if (this.containsKey(resourceKey)) {
            val original = super.get(resourceKey)
            val id = super.getId(original)

            return super.registerMapping(id, resourceKey, obj, lifecycle)
        }

        return super.register(resourceKey, obj, lifecycle)
    }
}