package xyz.bluspring.onthequest.jewel

import com.mojang.datafixers.util.Pair
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.EquipmentSlot as VanillaEquipmentSlot
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect

class InvisibilityJewelType(
    id: NamespacedKey,
    effects: List<PotionEffect>,
    modelId: Int,
    slots: List<EquipmentSlot> = listOf(),
    materials: List<Material> = listOf(),
    effectsWhenHeld: Boolean = false,
    probability: Double = 0.0
) : JewelType(
    id, effects, modelId, slots, materials, effectsWhenHeld, probability
) {
    override fun apply(player: Player) {
        super.apply(player)

        val appliedSlots = slots.map { VanillaEquipmentSlot.valueOf(it.name) }.filter { player.inventory.armorContents[it.index] != null }
        if (appliedSlots.isNotEmpty()) {
            if (player.inventory.armorContents.size != appliedSlots.size)
                return

            val packet = ClientboundSetEquipmentPacket(
                player.entityId,
                appliedSlots.map {
                    Pair(it, ItemStack.EMPTY)
                }
            )

            player.world.players.forEach {
                val connection = (it as CraftPlayer).handle.connection
                connection.send(packet)
            }
        }
    }

    fun removeHiddenArmorEffects(player: Player) {
        val appliedSlots = slots.map { VanillaEquipmentSlot.valueOf(it.name) }.filter { player.inventory.armorContents[it.index] != null }
        if (appliedSlots.isNotEmpty()) {
            if (player.inventory.armorContents.size != appliedSlots.size)
                return

            val packet = ClientboundSetEquipmentPacket(
                player.entityId,
                appliedSlots.map {
                    Pair(it, (player.inventory.armorContents[it.ordinal] as CraftItemStack).handle)
                }
            )

            player.world.players.forEach {
                val connection = (it as CraftPlayer).handle.connection
                connection.send(packet)
            }
        }
    }

    override fun remove(player: Player) {
        super.remove(player)

        removeHiddenArmorEffects(player)
    }
}