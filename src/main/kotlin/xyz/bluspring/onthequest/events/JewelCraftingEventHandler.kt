package xyz.bluspring.onthequest.events

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType
import io.papermc.paper.event.block.BeaconActivatedEvent
import io.papermc.paper.event.block.BeaconDeactivatedEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
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
    private val beacons = mutableSetOf<Location>()
    private val beaconsToRemove = mutableListOf<Location>()

    init {
        OnTheQuest.plugin.server.scheduler.runTaskTimer(OnTheQuest.plugin, Runnable {
            beacons.forEach {
                if (!it.isChunkLoaded) {
                    beaconsToRemove.add(it)
                    return@forEach
                }

                scanBeaconForJewels(it)
            }

            // Due to concurrency issues, we need to remove it here.
            beacons.removeAll(beaconsToRemove.toSet())
            beaconsToRemove.clear()
        }, 0L, 35L)
    }

    private fun scanBeaconForJewels(pos: Location) {
        val entities = pos.getNearbyEntitiesByType(Item::class.java, 1.75, 4.5)

        if (entities.isEmpty())
            return

        val entitiesToRemove = mutableListOf<Item>()
        val foundJewelTypes = mutableSetOf<JewelType>()
        entities.forEach {
            val stack = it.itemStack

            if (!stack.hasItemMeta())
                return@forEach

            val meta = stack.itemMeta
            if (!meta.persistentDataContainer.has(Jewels.JEWEL_TYPE_KEY))
                return@forEach

            val jewelTypeId = NamespacedKey.fromString(meta.persistentDataContainer.get(Jewels.JEWEL_TYPE_KEY, PersistentDataType.STRING)!!) ?: return@forEach
            val jewelType = Jewels.REGISTRY.get(jewelTypeId) ?: return@forEach

            if (jewelType == Jewels.AVATAR)
                return@forEach

            if (!foundJewelTypes.contains(jewelType)) {
                foundJewelTypes.add(jewelType)
                entitiesToRemove.add(it)
            }
        }

        if (foundJewelTypes.size < Jewels.REGISTRY.toList().size - 1)
            return

        pos.world.spawnParticle(Particle.END_ROD, pos, 75, .05, .02, .05, .1)

        pos.block.type = Material.AIR

        pos.world.playSound(pos, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, .4F, 1.6F)
        pos.world.playSound(pos, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 2.4F, 1.0F)
        pos.world.playSound(pos, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 2.4F, 1.1F)

        beaconsToRemove.add(pos)

        entitiesToRemove.forEach {
            it.remove()
        }

        val avatarItemEntity = pos.world.spawnEntity(pos.clone().add(0.0, 0.5, 0.0), EntityType.DROPPED_ITEM, false) as Item
        val avatarItem = Jewels.AVATAR.getItem(1)

        avatarItemEntity.itemStack = avatarItem
    }

    @EventHandler
    fun onBeaconActivate(ev: BeaconActivatedEvent) {
        val pos = ev.block.location

        // theoretically, this shouldn't run more than once before a BeaconDeactivatedEvent happens.
        beacons.add(pos)

        scanBeaconForJewels(pos)
    }

    @EventHandler
    fun onBeaconDeactivate(ev: BeaconDeactivatedEvent) {
        val pos = ev.block.location
        beacons.removeIf { it.blockX == pos.blockX && it.blockY == pos.blockY && it.blockZ == pos.blockZ }
    }
}