package xyz.bluspring.onthequest.jewel

import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect

open class JewelType(
    private val id: NamespacedKey,
    val effects: List<PotionEffect>,
    val modelId: Int,
    val slots: List<EquipmentSlot> = listOf(),
    val materials: List<Material> = listOf(),
    val effectsWhenHeld: Boolean = false
) : Keyed {
    override fun getKey(): NamespacedKey {
        return id
    }

    open fun apply(player: Player) {
        player.addPotionEffects(effects)
    }
}