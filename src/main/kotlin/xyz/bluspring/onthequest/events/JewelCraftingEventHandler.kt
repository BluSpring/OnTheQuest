package xyz.bluspring.onthequest.events

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import xyz.bluspring.onthequest.jewel.JewelType
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.DataContainerUtil
import xyz.bluspring.onthequest.util.StringArrayDataType

class JewelCraftingEventHandler : Listener {
    @EventHandler
    fun onAnvilCraft(ev: PrepareAnvilEvent) {
        if (ev.inventory.secondItem != null && ev.inventory.firstItem != null) {
            val item = ev.inventory.secondItem!!

            if (!item.hasItemMeta() || !item.itemMeta.persistentDataContainer.has(Jewels.JEWEL_TYPE_KEY))
                return

            val jewelTypes = DataContainerUtil.parseKeys(Jewels.JEWEL_TYPE_KEY, item.itemMeta.persistentDataContainer)

            var resultItem = ev.inventory.firstItem!!

            for (jewelTypeKey in jewelTypes) {
                val jewelType = Jewels.REGISTRY.get(jewelTypeKey) ?: continue

                val armorSlot = SlotType.getByMaterial(ev.inventory.firstItem!!.type)
                if (armorSlot != null) {
                    val equipmentSlot = EquipmentSlot.valueOf(armorSlot.name)

                    if (jewelType.slots.contains(equipmentSlot)) {
                        resultItem = applyJewelToItem(ev.inventory.firstItem!!, jewelType)
                    }
                }

                if (jewelType.materials.contains(ev.inventory.firstItem!!.type)) {
                    resultItem = applyJewelToItem(ev.inventory.firstItem!!, jewelType)
                }
            }

            ev.result = resultItem
        }
    }

    private fun applyJewelToItem(stack: ItemStack, jewelType: JewelType): ItemStack {
        val itemStack = stack.clone()

        val meta = itemStack.itemMeta
        val list = DataContainerUtil.parseKeys(Jewels.JEWEL_TYPE_KEY, meta.persistentDataContainer).toMutableList()

        list.add(jewelType.key)

        meta.persistentDataContainer.set(Jewels.JEWEL_TYPE_KEY, StringArrayDataType(), list.map { it.toString() }.toTypedArray())

        meta.lore(mutableListOf<Component>().apply {
            list.forEach {
                add(Component.translatable("item.${it.namespace}.${it.key}").color(NamedTextColor.DARK_PURPLE))
            }
        })

        itemStack.itemMeta = meta

        return itemStack
    }

    // Beacon crafting handles
}