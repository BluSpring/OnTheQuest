package xyz.bluspring.onthequest.jewel

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.util.MappedRegistry
import xyz.bluspring.onthequest.util.ToolType

object Jewels {
    val REGISTRY: Registry<JewelType> = MappedRegistry()
    val JEWEL_TYPE_KEY = NamespacedKey("questsmp", "jewel_type")

    val EARTH = register(
        JewelType(
            key("earth_jewel"),
            listOf(),
            15,
            materials = ToolType.HOE.items
        )
    )

    val STRENGTH = register(
        JewelType(
            key("strength_jewel"),
            listOf(
                PotionEffect(PotionEffectType.INCREASE_DAMAGE, 15, 0)
            ),
            20,
            materials = mutableListOf<Material>().apply {
                addAll(ToolType.SWORD.items)
                addAll(ToolType.AXE.items)
            }
        )
    )

    // earth 15 /
    // fireres 16 /
    // haste 17 /
    // invis 18 /
    // speed 19 /
    // strength 20 /
    // avatar 21

    val SPEED = register(
        JewelType(
            key("speed_jewel"),
            listOf(
                PotionEffect(PotionEffectType.SPEED, 15, 1)
            ),
            19,
            slots = listOf(
                EquipmentSlot.FEET
            )
        )
    )

    val FIRE_RESISTANCE = register(
        JewelType(
            key("fire_resistance_jewel"),
            listOf(
                PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5, 0)
            ),
            16,
            slots = listOf(
                EquipmentSlot.HEAD
            )
        )
    )

    val HASTE = register(
        JewelType(
            key("haste_jewel"),
            listOf(
                PotionEffect(PotionEffectType.FAST_DIGGING, 15, 1)
            ),
            17,
            materials = ToolType.PICKAXE.items
        )
    )

    val INVISIBILITY = register(
        JewelType(
            key("invisibility_jewel"),
            listOf(
                PotionEffect(PotionEffectType.INVISIBILITY, 5, 0)
            ),
            18,
            slots = listOf(EquipmentSlot.CHEST)
        )
    )

    val AVATAR = register(
        AvatarJewelType(
            key("avatar_jewel"),
            21,
            effectsWhenHeld = true
        )
    )

    private fun key(path: String): NamespacedKey {
        return NamespacedKey("questsmp", path)
    }

    fun register(value: JewelType): JewelType {
        (REGISTRY as MappedRegistry<JewelType>).register(value.key, value)
        return value
    }
}