package xyz.bluspring.onthequest.data.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Items
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.jewel.Jewel
import xyz.bluspring.onthequest.data.jewel.JewelManager

class JewelReloaderItem : CustomItem(Items.BLAZE_ROD, 1) {
    override fun use(player: Player, event: PlayerInteractEvent) {
        if (!event.action.isRightClick)
            return

        val old = JewelManager.getOrCreateJewel(player)
        var jewel: Jewel

        val random = RandomSource.create()

        // Make sure we don't just keep getting the same jewel
        do {
            jewel = QuestRegistries.JEWEL.getRandom(random).get().value()
        } while (jewel.id == old.id)

        JewelManager.replaceOldJewel(player, jewel, true)
        JewelManager.setJewel(player, jewel)
        event.item!!.subtract(1)
        player.sendMessage(
            Component.text(">> ")
                .color(NamedTextColor.GREEN)
                .append(
                    Component.text(" Your jewel has been changed to the ")
                        .append(Component.translatable(jewel.translationKey).color(jewel.color))
                        .append(Component.text("."))
                )
        )
    }
}