package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.MappedRegistry

object JewelAbilities {
    val REGISTRY: Registry<JewelAbility> = MappedRegistry()

    val LIFE_PRIMARY = register(LifeJewelAbility())

    internal fun key(path: String): NamespacedKey {
        return NamespacedKey("questsmp", path)
    }

    fun register(value: JewelAbility): JewelAbility {
        (Jewels.REGISTRY as MappedRegistry<JewelAbility>).register(value.key, value)
        OnTheQuest.plugin.server.pluginManager.registerEvents(value, OnTheQuest.plugin)
        return value
    }
}