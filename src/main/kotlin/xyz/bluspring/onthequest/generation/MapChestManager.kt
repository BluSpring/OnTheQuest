package xyz.bluspring.onthequest.generation

import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.item.MapItem
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import org.bukkit.*
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootTable
import org.bukkit.loot.LootTables
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.util.Chances
import java.util.UUID
import kotlin.random.Random

object MapChestManager {
    val MAP_ID_KEY = NamespacedKey("questsmp", "map_id")
    val LOOT_TABLE_CHANCES = mapOf(
        LootTables.BASTION_TREASURE to Chances.MEDIUM,

        LootTables.ANCIENT_CITY to Chances.MEDIUM,
        LootTables.ANCIENT_CITY_ICE_BOX to Chances.MEDIUM,

        LootTables.NETHER_BRIDGE to Chances.LOW,

        LootTables.STRONGHOLD_CORRIDOR to Chances.LOW,
        LootTables.STRONGHOLD_CROSSING to Chances.LOW,
        LootTables.STRONGHOLD_LIBRARY to Chances.LOW,

        LootTables.BURIED_TREASURE to Chances.LOW,

        LootTables.VILLAGE_TANNERY to Chances.SUPER_LOW,

        LootTables.PILLAGER_OUTPOST to Chances.SUPER_LOW,

        LootTables.SIMPLE_DUNGEON to Chances.SUPER_LOW,

        LootTables.WOODLAND_MANSION to Chances.HIGH,

        LootTables.RUINED_PORTAL to Chances.SUPER_LOW,

        LootTables.JUNGLE_TEMPLE to Chances.MEDIUM,

        LootTables.DESERT_PYRAMID to Chances.SUPER_LOW,

        LootTables.SHIPWRECK_TREASURE to Chances.SUPER_LOW,
        LootTables.SHIPWRECK_MAP to Chances.SUPER_LOW,

        LootTables.IGLOO_CHEST to Chances.LOW,

        LootTables.UNDERWATER_RUIN_SMALL to Chances.SUPER_LOW,
        LootTables.UNDERWATER_RUIN_BIG to Chances.SUPER_LOW,

        LootTables.END_CITY_TREASURE to Chances.MEDIUM
    )

    fun generate(player: Player): ItemStack {
        val uuid = UUID.randomUUID()

        val pos = generateChest(uuid, player.world, player.location)
        val blockPos = BlockPos(pos.blockX, pos.blockY, pos.blockZ)

        // Create item
        val nmsLevel = (player.world as CraftWorld).handle
        val nmsStack = MapItem.create(nmsLevel, pos.blockX, pos.blockZ, 2, true, true)
        MapItem.renderBiomePreviewMap(nmsLevel, nmsStack)
        MapItemSavedData.addTargetDecoration(nmsStack, blockPos, "+", MapDecoration.Type.TARGET_X)

        val stack = nmsStack.bukkitStack
        val meta = stack.itemMeta
        meta.setCustomModelData(15)

        val container = meta.persistentDataContainer
        container.set(MAP_ID_KEY, PersistentDataType.STRING, uuid.toString())

        stack.itemMeta = meta

        return stack
    }

    fun generateChest(uuid: UUID, world: World, loc: Location): Location {
        // mix the seed to avoid accidental collisions
        val seed = ((world.seed shl 7 or loc.blockX.toLong() + loc.blockZ or loc.blockY.toLong()) xor loc.yaw.toLong()) + Random.nextLong()
        val random = Random(seed)

        var location: Location? = null
        var foundPos = false
        do {
            val x = random.nextInt(loc.blockX - 2048, loc.blockX + 2048)
            val z = random.nextInt(loc.blockZ - 2048, loc.blockZ + 2048)

            val chunkX = x / 16
            val chunkZ = z / 16

            val chunk = world.getChunkAt(chunkX, chunkZ)

            for (y in random.nextInt(-53, 37)..42) {
                val block = chunk.getBlock(x and 0xF, y, z and 0xF)
                val checkLoc = block.location.clone()

                if (
                    checkLoc.add(0.0, 1.0, 0.0).block.isSolid
                    && checkLoc.add(0.0, -1.0, 0.0).block.isSolid
                    && checkLoc.add(1.0, 0.0, 0.0).block.isSolid
                    && checkLoc.add(-1.0, 0.0, 0.0).block.isSolid
                    && checkLoc.add(0.0, 0.0, 1.0).block.isSolid
                    && checkLoc.add(0.0, 0.0, -1.0).block.isSolid
                ) {
                    foundPos = true

                    location = block.location
                    break
                }
            }
        } while (!foundPos)

        if (location == null)
            throw IllegalStateException("how the fuck")

        location.block.type = Material.CHEST
        location.block.setMetadata("questsmp_map_chest", MapChestMetadata(uuid))

        if (OnTheQuest.debug)
            Bukkit.broadcast(Component.text("[OnTheQuest DEBUG] New chest generated at ${location.blockX} ${location.blockY} ${location.blockZ}"))

        return location
    }
}