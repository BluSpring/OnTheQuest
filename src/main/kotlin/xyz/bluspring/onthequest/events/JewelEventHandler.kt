package xyz.bluspring.onthequest.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_19_R1.advancement.CraftAdvancement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import xyz.bluspring.onthequest.data.jewel.JewelManager
import xyz.bluspring.onthequest.data.quests.QuestManager
import xyz.bluspring.onthequest.util.KotlinHelper

class JewelEventHandler : Listener {
    @EventHandler
    fun onAdvancementComplete(ev: PlayerAdvancementDoneEvent) {
        val jewel = JewelManager.getOrCreateJewel(ev.player)
        val nmsAdvancement = (ev.advancement as CraftAdvancement).handle

        if (!QuestManager.isAdvancementAQuest(nmsAdvancement, jewel))
            return

        val level = JewelManager.getOrCreateLevel(ev.player)

        if (level >= jewel.maxLevel) {
            ev.player.sendMessage("${ChatColor.RED}You have reached the max level of your jewel, so this quest does not count towards your jewel level.")
            return
        }

        ev.player.sendMessage(">> Your jewel has levelled up to ${level + 1}!")
        JewelManager.addToLevel(ev.player, 1)
    }

    @EventHandler
    fun onPlayerJoin(ev: PlayerJoinEvent) {
        val jewel = JewelManager.getOrCreateJewel(ev.player)
        val level = JewelManager.getOrCreateLevel(ev.player)

        ev.player.sendMessage(
            Component.text(">>")
                .color(NamedTextColor.GREEN)
                .append(Component.text("You currently have the "))
                .append(
                    Component
                        .translatable(jewel.translationKey)
                        .color(jewel.color)
                )
                .append(Component.text(" at "))
                .append(Component.text("Level $level").color(NamedTextColor.YELLOW))
                .append(Component.text("."))
        )
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeath(ev: PlayerDeathEvent) {
        if (ev.isCancelled)
            return

        ev.drops.removeIf { JewelManager.isJewel(it) }

        val jewel = JewelManager.getOrCreateJewel(ev.player)
        val newLevel = JewelManager.addToLevel(ev.player, -1)
        ev.player.sendMessage("${ChatColor.RED}>> You have died, and so your jewel level has dropped to ${ChatColor.GOLD}Level $newLevel${ChatColor.RED}!")

        if (newLevel < jewel.minLevel) {
            ev.player.spigot().respawn()

            KotlinHelper.delayByOneTick {
                ev.player.banPlayer("Your jewel level has dropped below Level ${jewel.minLevel}!")
            }
        }
    }

    @EventHandler
    fun onPlayerRespawn(ev: PlayerRespawnEvent) {
        val jewel = JewelManager.getOrCreateJewel(ev.player)
        JewelManager.setJewel(ev.player, jewel)
    }
}