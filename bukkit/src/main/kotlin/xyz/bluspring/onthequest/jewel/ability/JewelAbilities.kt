package xyz.bluspring.onthequest.jewel.ability

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import xyz.bluspring.onthequest.OnTheQuestBukkit
import xyz.bluspring.onthequest.util.MappedRegistry

object JewelAbilities {
    val REGISTRY: Registry<JewelAbility> = MappedRegistry()

    val DRAGON_PRIMARY = register(DragonJewelPrimaryAbility())
    val DRAGON_SECONDARY = register(DragonJewelSecondaryAbility())

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
        OnTheQuestBukkit.plugin.server.pluginManager.registerEvents(value, OnTheQuestBukkit.plugin)
        return value
    }
}