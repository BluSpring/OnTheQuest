package xyz.bluspring.onthequest.jewel

import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class AttributedJewelType(
    id: NamespacedKey,
    modelId: Int,
    private val attributes: Map<Attribute, AttributeModifier>,
    slots: List<EquipmentSlot> = listOf(),
    materials: List<Material> = listOf(),
    probability: Double = 0.0
) : JewelType(
    id, listOf(), modelId,
    slots, materials,
    probability = probability
) {
    override fun apply(player: Player) {
        val craftPlayer = (player as CraftPlayer).handle

        attributes.forEach { (attr, modifier) ->
            if (!craftPlayer.attributes.hasAttribute(attr))
                craftPlayer.attributes.registerAttribute(attr)

            val attrInstance = craftPlayer.attributes.getInstance(attr) ?: return@forEach

            attrInstance.addTransientModifier(modifier)
        }
    }

    override fun remove(player: Player) {
        val craftPlayer = (player as CraftPlayer).handle

        attributes.forEach { (attr, modifier) ->
            if (!craftPlayer.attributes.hasAttribute(attr))
                return@forEach

            val attrInstance = craftPlayer.attributes.getInstance(attr) ?: return@forEach

            attrInstance.removeModifier(modifier)
        }
    }
}