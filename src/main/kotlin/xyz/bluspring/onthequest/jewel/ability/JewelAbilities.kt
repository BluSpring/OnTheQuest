package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.MappedRegistry

object JewelAbilities {
    val REGISTRY: Registry<JewelAbility> = MappedRegistry()

    val LIFE_PRIMARY = register(LifeJewelAbility())

    val ICE_PRIMARY = register(IceJewelPrimaryAbility())
    val ICE_SECONDARY = register(IceJewelSecondaryAbility())

    val SKELETAL_PRIMARY = register(SkeletalJewelAbility())

    val VOID_PRIMARY = register(VoidJewelAbility())

    val WATER_PRIMARY = register(WaterJewelAbility())

    internal fun key(path: String): NamespacedKey {
        return NamespacedKey("questsmp", path)
    }

    fun register(value: JewelAbility): JewelAbility {
        (REGISTRY as MappedRegistry<JewelAbility>).register(value.key, value)
        OnTheQuest.plugin.server.pluginManager.registerEvents(value, OnTheQuest.plugin)
        return value
    }
}