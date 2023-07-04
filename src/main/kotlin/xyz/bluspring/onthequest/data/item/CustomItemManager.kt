package xyz.bluspring.onthequest.data.item

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import xyz.bluspring.onthequest.data.QuestRegistries

object CustomItemManager {
    val REVIVE = register("revive", ReviveItem())
    val JEWEL_RELOADER = register("jewel_reloader", JewelReloaderItem())

    fun getCustomItem(stack: ItemStack): CustomItem? {
        return QuestRegistries.CUSTOM_ITEM.firstOrNull { it.isEqual(stack) }
    }

    private fun register(id: String, item: CustomItem): CustomItem {
        return Registry.register(QuestRegistries.CUSTOM_ITEM, ResourceLocation("questsmp", id), item)
    }
}