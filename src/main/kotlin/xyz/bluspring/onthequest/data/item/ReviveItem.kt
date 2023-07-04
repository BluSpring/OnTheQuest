package xyz.bluspring.onthequest.data.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.item.Items
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.bluspring.onthequest.OnTheQuest

class ReviveItem : CustomItem(Items.BLAZE_ROD, 2) {
    private val plugin = OnTheQuest.plugin

    override fun use(player: Player, event: PlayerInteractEvent) {
        if (!event.action.isRightClick)
            return

        val inventory = Bukkit.createInventory(player, 54)

        val banList = Bukkit.getBanList(BanList.Type.NAME)
        this.plugin.server.bannedPlayers.forEach {
            if (it.name == null)
                return

            val entry = banList.getBanEntry(it.name!!) ?: return@forEach
            if (entry.source != "questsmp_level_ban")
                return@forEach

            val item = ItemStack(Material.PLAYER_HEAD, 1)

            val meta = item.itemMeta!! as SkullMeta
            meta.owningPlayer = it
            meta.displayName(Component.text("Revive ${it.name}").color(NamedTextColor.GREEN))

            item.itemMeta = meta

            inventory.addItem(item)
        }

        val selector = ReviveSelector(player, inventory, event.item!!)
        this.plugin.server.pluginManager.registerEvents(selector, this.plugin)
        player.openInventory(inventory)
    }

    private inner class ReviveSelector(private val player: Player, private val inventory: Inventory, private val item: ItemStack) : Listener {
        @EventHandler
        fun onInventoryInteract(ev: InventoryInteractEvent) {
            if (ev.inventory != inventory)
                return

            ev.isCancelled = true
            ev.result = Event.Result.DENY
        }

        @EventHandler
        fun onMoveItem(ev: InventoryMoveItemEvent) {
            ev.isCancelled = true
        }

        @EventHandler
        fun onInventoryClick(ev: InventoryClickEvent) {
            ev.isCancelled = true
            ev.result = Event.Result.DENY

            if (ev.clickedInventory != inventory)
                return

            if (ev.currentItem != null || ev.cursor != null) {
                val item = ev.currentItem ?: ev.cursor ?: return // i never know what to use

                if (!item.hasItemMeta())
                    return

                if (item.itemMeta !is SkullMeta)
                    return

                val meta = item.itemMeta as SkullMeta

                if (meta.owningPlayer == null)
                    return

                Bukkit.getBanList(BanList.Type.NAME).pardon(meta.owningPlayer!!.name!!)

                ev.whoClicked.sendMessage("${ChatColor.YELLOW}>> ${ChatColor.WHITE}Revived player ${ChatColor.GREEN}${meta.owningPlayer!!.name}${ChatColor.WHITE}.")
                Bukkit.getScheduler().runTask(OnTheQuest.plugin, Runnable {
                    this.item.subtract()
                    ev.whoClicked.closeInventory()
                })
            }
        }

        @EventHandler
        fun onInventoryClose(ev: InventoryCloseEvent) {
            if (ev.player != player)
                return

            if (ev.inventory != inventory)
                return

            HandlerList.unregisterAll(this)
        }
    }
}