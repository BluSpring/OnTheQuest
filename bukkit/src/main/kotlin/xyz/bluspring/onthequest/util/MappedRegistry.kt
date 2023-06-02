package xyz.bluspring.onthequest.util

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Registry

class MappedRegistry<T : Keyed> : Registry<T> {
    private val map = mutableMapOf<NamespacedKey, T>()

    fun keys(): Collection<NamespacedKey> {
        return map.keys
    }

    override fun iterator(): MutableIterator<T> {
        return map.values.iterator()
    }

    override fun get(key: NamespacedKey): T? {
        return map[key]
    }

    fun register(key: NamespacedKey, value: T) {
        map[key] = value
    }
}