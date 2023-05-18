package xyz.bluspring.onthequest.events

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.jewel.JewelType
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.DataContainerUtil
import xyz.bluspring.onthequest.util.KotlinHelper.ticks
import xyz.bluspring.onthequest.util.StringArrayDataType
import java.io.File
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class JewelEffectEventHandler : Listener {
    init {
        OnTheQuest.plugin.server.scheduler.runTaskTimer(OnTheQuest.plugin, Runnable {
            activeJewels.forEach { (player, jewels) ->
                jewels.forEach { jewel ->
                    jewel.apply(player)
                }
            }
        }, 0L, 10L) // don't need to run this all the damn time
    }

    @EventHandler
    fun onPlayerLogin(ev: PlayerJoinEvent) {
        applyJewelEffectsFromInventory(ev.player)
    }

    @EventHandler
    fun onPlayerDeath(ev: PlayerDeathEvent) {
        activeJewels[ev.player]?.forEach {
            it.remove(ev.player)
        }

        activeJewels.remove(ev.player)
    }

    @EventHandler
    fun onPlayerRespawn(ev: PlayerRespawnEvent) {
        applyJewelEffectsFromInventory(ev.player)
    }

    private fun applyJewelEffectsFromInventory(player: Player) {
        if (!activeJewels.contains(player))
            activeJewels[player] = mutableSetOf()
        else {
            activeJewels[player]!!.forEach {
                it.remove(player)
            }

            activeJewels.remove(player)
            activeJewels[player] = mutableSetOf()
        }

        player.inventory.armorContents.forEach {
            if (it == null)
                return@forEach

            val jewelTypes = getJewelTypes(it) ?: return@forEach
            if (jewelTypes.isEmpty())
                return@forEach

            activeJewels[player]!!.addAll(jewelTypes)
        }

        val mainHand = player.inventory.itemInMainHand
        val mainHandJewelTypes = getJewelTypes(mainHand)

        if (mainHandJewelTypes != null) {
            activeJewels[player]!!.addAll(mainHandJewelTypes)
        }

        val offHand = player.inventory.itemInOffHand
        val offHandJewelTypes = getJewelTypes(offHand)
        if (offHandJewelTypes != null) {
            activeJewels[player]!!.addAll(offHandJewelTypes)
        }
    }

    @EventHandler
    fun onPlayerDisconnect(ev: PlayerQuitEvent) {
        activeJewels[ev.player]?.forEach {
            it.remove(ev.player)
        }

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

                jewelTypes.forEach {
                    it.remove(ev.player)
                }

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

        jewelTypes.forEach {
            it.remove(player)
        }
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

    // i fucking hate bukkit with a passion
    @EventHandler
    fun onInventoryClick(ev: InventoryClickEvent) {
        if (ev.whoClicked is Player) {
            if (!activeJewels.contains(ev.whoClicked))
                activeJewels[ev.whoClicked as Player] = mutableSetOf()

            if (ev.slotType == InventoryType.SlotType.QUICKBAR) {
                if (ev.slot != ev.whoClicked.inventory.heldItemSlot)
                    return

                if (ev.currentItem != null) {
                    val jewelTypes = getJewelTypes(ev.currentItem!!) ?: return

                    jewelTypes.forEach {
                        if (it.effectsWhenHeld) {
                            it.remove(ev.whoClicked as Player)
                            activeJewels[ev.whoClicked]!!.remove(it)
                        }
                    }
                }

                if (ev.cursor != null) {
                    val jewelTypes = getJewelTypes(ev.cursor!!) ?: return

                    jewelTypes.forEach {
                        if (it.effectsWhenHeld)
                            activeJewels[ev.whoClicked]!!.add(it)
                    }
                }
            } else if (ev.slotType == InventoryType.SlotType.ARMOR) {
                if (ev.currentItem != null) {
                    do {
                        val jewelTypes = getJewelTypes(ev.currentItem!!) ?: return
                        val slot = SlotType.getByMaterial(ev.currentItem!!.type) ?: break
                        val eqSlot = EquipmentSlot.valueOf(slot.name)

                        jewelTypes.forEach {
                            if (it.slots.contains(eqSlot)) {
                                it.remove(ev.whoClicked as Player)
                                activeJewels[ev.whoClicked]!!.remove(it)
                            }
                        }
                    } while (false)
                }

                if (ev.cursor != null) {
                    val jewelTypes = getJewelTypes(ev.cursor!!) ?: return
                    val slot = SlotType.getByMaterial(ev.currentItem!!.type) ?: return
                    val eqSlot = EquipmentSlot.valueOf(slot.name)

                    jewelTypes.forEach {
                        if (it.slots.contains(eqSlot))
                            activeJewels[ev.whoClicked]!!.add(it)
                    }
                }
            }
        }
    }

    private val farmable = listOf(
        Material.WHEAT,
        Material.MELON,
        Material.GRASS,
        Material.TALL_GRASS,
        Material.POTATOES,
        Material.CARROTS,
        Material.BEETROOTS,
        Material.PUMPKIN,
        Material.MUSHROOM_STEM,
        Material.BROWN_MUSHROOM,
        Material.BROWN_MUSHROOM_BLOCK,
        Material.RED_MUSHROOM,
        Material.RED_MUSHROOM_BLOCK
    )

    @EventHandler
    fun onCropBreak(ev: BlockDropItemEvent) {
        if (!activeJewels.contains(ev.player))
            return

        if (!activeJewels[ev.player]!!.contains(Jewels.EARTH) && !activeJewels[ev.player]!!.contains(Jewels.AVATAR))
            return

        val type = ev.blockState.type

        if (!farmable.contains(type))
            return

        ev.items.forEach {
            it.itemStack.amount = Random.nextInt(it.itemStack.amount, it.itemStack.amount + 5)
        }
    }

    @EventHandler
    fun onPlayerSwapItems(ev: PlayerSwapHandItemsEvent) {
        applyJewelEffectsFromInventory(ev.player)
    }

    // this is what you call someone who is incredibly done with Bukkit's bullshit
    @EventHandler
    fun onPlayerCloseInventory(ev: InventoryCloseEvent) {
        applyJewelEffectsFromInventory(ev.player as Player)
    }

    @EventHandler
    fun onPlayerDropItem(ev: PlayerDropItemEvent) {
        val droppedStack = ev.itemDrop.itemStack

        if (droppedStack.hasItemMeta() && droppedStack.itemMeta.persistentDataContainer.has(Jewels.JEWEL_TYPE_KEY))
            applyJewelEffectsFromInventory(ev.player)
    }

    @EventHandler
    fun onPlayerPickupItem(ev: EntityPickupItemEvent) {
        if (ev.entityType != EntityType.PLAYER)
            return

        val itemStack = ev.item.itemStack
        if (itemStack.hasItemMeta() && itemStack.itemMeta.persistentDataContainer.has(Jewels.JEWEL_TYPE_KEY))
            applyJewelEffectsFromInventory(ev.entity as Player)
    }

    companion object {
        private val activeJewels = mutableMapOf<Player, MutableSet<JewelType>>()
        /*private val tempJewels = mutableMapOf<UUID, MutableSet<Pair<Long, JewelType>>>()
        private val tempJewelStorage = File(OnTheQuest.plugin.dataFolder, "temp_jewels.json")

        init {
            load()

            Bukkit.getScheduler().runTaskTimerAsynchronously(OnTheQuest.plugin, Runnable {
                tempJewels.forEach { (uuid, )

                }
            }, 0L, 1.minutes.ticks.toLong())
        }

        private fun save() {
            if (!tempJewelStorage.exists())
                tempJewelStorage.createNewFile()

            val json = JsonObject()
            tempJewels.forEach { (uuid, jewels) ->
                json.add(uuid.toString(), JsonArray().apply {
                    jewels.forEach { (startTime, jewelType) ->
                        this.add(JsonObject().apply {
                            this.addProperty("start_time", startTime)
                            this.addProperty("jewel_type", jewelType.key.asString())
                        })
                    }
                })
            }

            tempJewelStorage.writeText(json.toString())
        }

        private fun load() {
            if (!tempJewelStorage.exists())
                return

            val json = JsonParser.parseString(tempJewelStorage.readText()).asJsonObject

            json.keySet().forEach { serializedUuid ->
                val uuid = UUID.fromString(serializedUuid)
                val jewels = json.get(serializedUuid).asJsonArray

                val set = mutableSetOf<Pair<Long, JewelType>>()

                jewels.forEach jewelCheck@{
                    val data = it.asJsonObject

                    val startTime = data.get("start_time").asLong
                    val jewelType = Jewels.REGISTRY.get(NamespacedKey.fromString(data.get("jewel_type").asString)!!) ?: return@jewelCheck

                    if (System.currentTimeMillis() - startTime >= 24.hours.inWholeMilliseconds)
                        return@jewelCheck

                    set.add(Pair(startTime, jewelType))
                }

                tempJewels[uuid] = set

                val player = Bukkit.getPlayer(uuid)
                if (player != null) {

                }
            }
        }*/

        fun getActiveJewels(player: Player): Set<JewelType>? {
            return activeJewels[player]
        }

        fun containsJewel(player: Player, jewelType: JewelType): Boolean {
            return getActiveJewels(player)?.contains(jewelType) == true || getActiveJewels(player)?.contains(Jewels.AVATAR) == true
        }

        /*fun applyTemporaryJewel(player: Player, jewel: JewelType) {
            if (!tempJewels.contains(player.uniqueId))
                tempJewels[player.uniqueId] = mutableSetOf()

            if (!activeJewels.contains(player))
                activeJewels[player] = mutableSetOf()

            tempJewels[player.uniqueId]!!.add(Pair(System.currentTimeMillis(), jewel))
            activeJewels[player]!!.add(jewel)

            save()
        }*/

        fun getJewelTypes(item: ItemStack): List<JewelType>? {
            if (!item.hasItemMeta())
                return null

            val meta = item.itemMeta
            val data = meta.persistentDataContainer

            val list = DataContainerUtil.parseKeys(Jewels.JEWEL_TYPE_KEY, data)

            return list.map { Jewels.REGISTRY.get(it)!! }
        }
    }
}