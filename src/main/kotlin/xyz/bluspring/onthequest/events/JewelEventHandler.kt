package xyz.bluspring.onthequest.events

import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_19_R1.advancement.CraftAdvancement
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import xyz.bluspring.onthequest.data.jewel.JewelManager
import xyz.bluspring.onthequest.data.quests.QuestManager

class JewelEventHandler : Listener {
    @EventHandler
    fun onAdvancementComplete(ev: PlayerAdvancementDoneEvent) {
        val jewel = JewelManager.getOrCreateJewel(ev.player)
        val nmsAdvancement = (ev.advancement as CraftAdvancement).handle

        if (!QuestManager.isAdvancementAQuest(nmsAdvancement, jewel))
            return

        val level = JewelManager.getOrCreateLevel(ev.player)

        if (level >= jewel.maxLevel) {
            ev.player.sendMessage("${ChatColor.RED}You have reached the max level of your jewel, and as such, this quest does not count towards your jewel level.")
            return
        }

        ev.player.sendMessage("Your jewel has levelled up to ${level + 1}!")
        JewelManager.addToLevel(ev.player, 1)
    }
}