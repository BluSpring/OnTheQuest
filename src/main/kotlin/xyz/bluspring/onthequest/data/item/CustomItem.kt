package xyz.bluspring.onthequest.data.item

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import xyz.bluspring.onthequest.data.QuestRegistries

abstract class CustomItem(
    val item: Item,
    val customModel: Int
) {
    open fun getTranslationKey(): String {
        val key = QuestRegistries.CUSTOM_ITEM.getKey(this) ?: return "failed to get registration key"
        return "item.${key.namespace}.${key.path}"
    }

    open fun getItem(count: Int = 1): ItemStack {
        val stack = ItemStack(item, count)
        stack.orCreateTag.putInt("CustomModelData", customModel)
        stack.hoverName = Component.translatable(getTranslationKey()).withStyle(Rarity.EPIC.color)

        return stack
    }

    open fun isEqual(stack: ItemStack): Boolean {
        return stack.item == item && stack.hasTag() && stack.tag?.getInt("CustomModelData") == customModel
    }

    abstract fun use(player: Player, event: PlayerInteractEvent)
}