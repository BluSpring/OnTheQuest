package xyz.bluspring.onthequest.jewel

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

open class AvatarJewelType(
    id: NamespacedKey,
    modelId: Int,
    slots: List<EquipmentSlot> = listOf(),
    materials: List<Material> = listOf(),
    effectsWhenHeld: Boolean = false
) : JewelType(id, listOf(), modelId, slots, materials, effectsWhenHeld) {
    override fun apply(player: Player) {
        Jewels.REGISTRY.forEach {
            if (it == this) // oops
                return@forEach

            it.apply(player)
        }
    }
}