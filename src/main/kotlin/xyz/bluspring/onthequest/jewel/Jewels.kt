package xyz.bluspring.onthequest.jewel

import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.jewel.ability.JewelAbilities
import xyz.bluspring.onthequest.util.Chances
import xyz.bluspring.onthequest.util.MappedRegistry
import xyz.bluspring.onthequest.util.ToolType

object Jewels {
    val REGISTRY: Registry<JewelType> = MappedRegistry()
    val JEWEL_TYPE_KEY = NamespacedKey("questsmp", "jewel_type")

    fun init() {
        // lol, he cried.
        // lmao, he sobbed.
    }

    val EARTH = register(
        JewelType(
            key("earth_jewel"),
            listOf(),
            15,
            materials = mutableListOf<Material>().apply {
                addAll(ToolType.HOE.items)
                addAll(ToolType.AXE.items)
            },
            probability = Chances.MEDIUM
        )
    )

    val STRENGTH = register(
        JewelType(
            key("strength_jewel"),
            listOf(
                PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20, 1)
            ),
            20,
            materials = mutableListOf<Material>().apply {
                addAll(ToolType.SWORD.items)
                addAll(ToolType.AXE.items)
            },
            probability = Chances.LOW
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
                PotionEffect(PotionEffectType.SPEED, 5 * 20, 1)
            ),
            19,
            slots = listOf(
                EquipmentSlot.FEET
            ),
            probability = Chances.MEDIUM
        )
    )

    val FIRE_RESISTANCE = register(
        JewelType(
            key("fire_resistance_jewel"),
            listOf(
                PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5 * 20, 0)
            ),
            16,
            slots = listOf(
                EquipmentSlot.HEAD
            ),
            probability = Chances.MEDIUM
        )
    )

    val HASTE = register(
        JewelType(
            key("haste_jewel"),
            listOf(
                PotionEffect(PotionEffectType.FAST_DIGGING, 5 * 20, 1)
            ),
            17,
            materials = ToolType.PICKAXE.items,
            probability = Chances.MEDIUM
        )
    )

    val INVISIBILITY = register(
        JewelType(
            key("invisibility_jewel"),
            listOf(
                PotionEffect(PotionEffectType.INVISIBILITY, 5 * 20, 0)
            ),
            18,
            slots = listOf(EquipmentSlot.CHEST),
            probability = Chances.MEDIUM
        )
    )

    val AVATAR = register(
        AvatarJewelType(
            key("avatar_jewel"),
            21,
            effectsWhenHeld = true
        )
    )

    val ICE = register(
        JewelType(
            key("ice_jewel"),
            listOf(),
            22,
            effectsWhenHeld = true
        ).apply {
            registerAbility(JewelAbilities.ICE_PRIMARY)
            registerAbility(JewelAbilities.ICE_SECONDARY)
        }
    )

    val LIFE = register(
        AttributedJewelType(
            key("life_jewel"),
            23,
            mapOf(
                Attributes.MAX_HEALTH to AttributeModifier("otq.lifeJewel", 2.0, AttributeModifier.Operation.ADDITION)
            ),
            slots = listOf(EquipmentSlot.CHEST)
        ).apply {
            registerAbility(JewelAbilities.LIFE_PRIMARY)
        }
    )

    val SKELETAL = register(
        JewelType(
            key("skeletal_jewel"),
            listOf(),
            24,
            effectsWhenHeld = true
        ).apply {
            registerAbility(JewelAbilities.SKELETAL_PRIMARY)
        }
    )

    val VOID = registerEvented(
        VoidJewelType(
            key("void_jewel"),
            25,
            listOf(EquipmentSlot.LEGS)
        ).apply {
            registerAbility(JewelAbilities.VOID_PRIMARY)
        }
    )

    val WATER = registerEvented(
        WaterJewelType(
            key("water_jewel"),
            26,
            listOf(EquipmentSlot.HEAD)
        ).apply {
            registerAbility(JewelAbilities.WATER_PRIMARY)
        }
    )

    private fun key(path: String): NamespacedKey {
        return NamespacedKey("questsmp", path)
    }

    fun register(value: JewelType): JewelType {
        (REGISTRY as MappedRegistry<JewelType>).register(value.key, value)
        return value
    }

    fun <T> registerEvented(value: T): JewelType where T : JewelType, T : Listener {
        this.register(value)
        OnTheQuest.plugin.server.pluginManager.registerEvents(value, OnTheQuest.plugin)

        return value
    }
}