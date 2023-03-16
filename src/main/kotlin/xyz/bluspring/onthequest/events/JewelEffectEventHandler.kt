package xyz.bluspring.onthequest.events

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.jewel.JewelType
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.DataContainerUtil
import xyz.bluspring.onthequest.util.StringArrayDataType

class JewelEffectEventHandler : Listener {
    private val activeJewels = mutableMapOf<Player, MutableSet<JewelType>>()

    init {
        OnTheQuest.plugin.server.scheduler.runTaskTimer(OnTheQuest.plugin, Runnable {
            activeJewels.forEach { (player, jewels) ->
                jewels.forEach { jewel ->
                    jewel.apply(player)
                }
            }
        }, 0L, 10L) // don't need to run this all the damn time
    }

    private fun getJewelTypes(item: ItemStack): List<JewelType>? {
        if (!item.hasItemMeta())
            return null

        val meta = item.itemMeta
        val data = meta.persistentDataContainer

        val list = DataContainerUtil.parseKeys(Jewels.JEWEL_TYPE_KEY, data)

        return list.map { Jewels.REGISTRY.get(it)!! }
    }

    @EventHandler
    fun onPlayerDisconnect(ev: PlayerQuitEvent) {
        activeJewels.remove(ev.player)
    }

    @EventHandler
    fun onItemHeldEvent(ev: PlayerItemHeldEvent) {
        val item = ev.player.inventory.getItem(ev.newSlot)
        val oldItem = ev.player.inventory.getItem(ev.previousSlot)

        // The order of this is important, as there *is* that chance of the player
        // simply off-handing the item.

        // It's important that this is in a do...while statement, as it would allow for
        // us to break out of the code and go into the next if statement without
        // doing any looping and without making an entirely new method specifically for this.
        do {
            // Unregister previously held items
            if (oldItem != null) {
                val jewelTypes = getJewelTypes(oldItem) ?: break

                if (!activeJewels.contains(ev.player))
                    break

                activeJewels[ev.player]!!.removeAll(jewelTypes.toSet())
            }
        } while (false) // don't loop

        // Register newly held items
        if (item != null) {
            val jewelTypes = getJewelTypes(item) ?: return

            jewelTypes.forEach { jewelType ->
                if (jewelType.materials.contains(item.type) || jewelType.effectsWhenHeld) {
                    if (!activeJewels.contains(ev.player))
                        activeJewels[ev.player] = mutableSetOf()

                    activeJewels[ev.player]!!.add(jewelType)
                }
            }
        }
    }

    // Armor handling, because Bukkit has like 12 different events just to deal with armor.
    private fun tryAddArmorJewel(player: Player, item: ItemStack, slot: EquipmentSlot) {
        val jewelTypes = getJewelTypes(item) ?: return

        jewelTypes.forEach { jewelType ->
            if (jewelType.materials.contains(item.type) || jewelType.slots.contains(slot)) {
                if (!activeJewels.contains(player))
                    activeJewels[player] = mutableSetOf()

                activeJewels[player]!!.add(jewelType)
            }
        }
    }

    private fun tryRemoveArmorJewel(player: Player, item: ItemStack) {
        val jewelTypes = getJewelTypes(item) ?: return

        if (!activeJewels.contains(player))
            return

        activeJewels[player]!!.removeAll(jewelTypes.toSet())
    }

    @EventHandler
    fun onArmorChangeEvent(ev: PlayerArmorChangeEvent) {
        val oldItem = ev.oldItem

        if (oldItem != null) {
            tryRemoveArmorJewel(ev.player, oldItem)
        }

        val newItem = ev.newItem

        if (newItem != null) {
            tryAddArmorJewel(ev.player, newItem, EquipmentSlot.valueOf(ev.slotType.name))
        }
    }

    @EventHandler
    fun onArmorDispenserEquipEvent(ev: BlockDispenseArmorEvent) {
        if (ev.targetEntity !is Player)
            return

        val slot = PlayerArmorChangeEvent.SlotType.getByMaterial(ev.item.type) ?: return
        tryAddArmorJewel(ev.targetEntity as Player, ev.item, EquipmentSlot.valueOf(slot.name))
    }

    @EventHandler
    fun onEquipViaAirClick(ev: PlayerInteractEvent) {
        if (ev.action.isRightClick && ev.hasItem()) {
            val item = ev.item!!

            val slot = PlayerArmorChangeEvent.SlotType.getByMaterial(item.type) ?: return
            val armorSlot = EquipmentSlot.valueOf(slot.name)

            val slotItem = ev.player.inventory.getItem(armorSlot)

            // I don't know if this event runs before or after equipping,
            // so let's just handle both to save time.
            if (slotItem == item || slotItem.type == Material.AIR)
                tryAddArmorJewel(ev.player, item, armorSlot)
        }
    }
}