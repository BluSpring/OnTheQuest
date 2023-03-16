package xyz.bluspring.onthequest.jewel

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
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

    open fun getItem(count: Int = 1): ItemStack {
        val itemStack = ItemStack(Material.EMERALD, count)

        val meta = itemStack.itemMeta

        meta.displayName(
            Component
                .translatable("item.${key.namespace}.${key.key}")
                .decoration(TextDecoration.ITALIC, false)
        )
        meta.setCustomModelData(modelId)
        meta.persistentDataContainer.set(Jewels.JEWEL_TYPE_KEY, PersistentDataType.STRING, id.toString())

        itemStack.itemMeta = meta

        return itemStack
    }
}