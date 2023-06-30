package xyz.bluspring.onthequest.events

import net.kyori.adventure.text.Component
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.LootGenerateEvent
import xyz.bluspring.onthequest.data.jewel.JewelManager
import xyz.bluspring.onthequest.data.quests.QuestCustomCriterias
import xyz.bluspring.onthequest.data.quests.QuestManager

class QuestEventHandler : Listener {
    @EventHandler
    fun onLootGenerated(ev: LootGenerateEvent) {
        if (ev.entity == null)
            return

        if (ev.entity !is Player)
            return

        val player = (ev.entity as CraftPlayer).handle
        QuestCustomCriterias.PLAYER_OPEN_NEW_LOOT_CHEST.trigger(player, ResourceLocation(ev.lootTable.key.toString()))
    }

    @EventHandler
    fun onPlayerDeath(ev: PlayerDeathEvent) {
        if (ev.isCancelled)
            return

        if (ev.player.lastDamageCause == null)
            return

        if (ev.player.lastDamageCause !is EntityDamageByEntityEvent)
            return

        if ((ev.player.lastDamageCause!! as EntityDamageByEntityEvent).damager !is Player)
            return

        ev.drops.add(QuestManager.getQuestCard(1))
    }

    @EventHandler
    fun onUseQuestCard(ev: PlayerInteractEvent) {
        if (!ev.hasItem())
            return

        if (ev.item == null)
            return

        if (!QuestManager.isQuestCard(ev.item!!))
            return

        val quests = QuestManager.getAllPossibleQuests(JewelManager.getOrCreateJewel(ev.player))
            .filter {
                !ev.player.getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.fromString(it.toString())!!)!!).isDone
                        && !ev.player.scoreboardTags.contains("otq_${it.path.replace("/", "_")}")
            }

        if (quests.isEmpty()) {
            ev.player.sendMessage("${ChatColor.RED} >> It appears that we are currently out of quests for you to complete, please try again later!")
            return
        }

        val questId = quests.random()
        val advancement = Bukkit.getAdvancement(NamespacedKey.fromString(questId.toString())!!)!!

        ev.player.addScoreboardTag("otq_${questId.path.replace("/", "_")}")
        ev.item!!.amount -= 1
        ev.player.sendMessage("${ChatColor.YELLOW} >> ${ChatColor.GREEN}New Quest: ")
        ev.player.sendMessage(Component.text(" - ").append(advancement.displayName()))
    }
}