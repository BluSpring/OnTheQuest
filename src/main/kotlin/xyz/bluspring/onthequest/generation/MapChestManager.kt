package xyz.bluspring.onthequest.generation

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.BlockPos
import net.minecraft.world.item.MapItem
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootTable
import org.bukkit.loot.LootTables
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.util.Chances
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

object MapChestManager {
    val MAP_ID_KEY = NamespacedKey("questsmp", "map_id")
    val LOOT_TABLE_CHANCES = mapOf(
        LootTables.BASTION_TREASURE.key to Chances.HIGH,

        LootTables.ANCIENT_CITY.key to Chances.HIGH,
        LootTables.ANCIENT_CITY_ICE_BOX.key to Chances.MEDIUM,

        LootTables.NETHER_BRIDGE.key to Chances.MEDIUM,

        LootTables.STRONGHOLD_CORRIDOR.key to Chances.MEDIUM,
        LootTables.STRONGHOLD_CROSSING.key to Chances.MEDIUM,
        LootTables.STRONGHOLD_LIBRARY.key to Chances.MEDIUM,

        LootTables.BURIED_TREASURE.key to Chances.LOW,

        LootTables.VILLAGE_TANNERY.key to Chances.SUPER_LOW,

        LootTables.PILLAGER_OUTPOST.key to Chances.SUPER_LOW,

        LootTables.SIMPLE_DUNGEON.key to Chances.SUPER_LOW,

        LootTables.WOODLAND_MANSION.key to Chances.VERY_HIGH,

        LootTables.RUINED_PORTAL.key to Chances.SUPER_LOW,

        LootTables.JUNGLE_TEMPLE.key to Chances.MEDIUM,

        LootTables.DESERT_PYRAMID.key to Chances.SUPER_LOW,

        LootTables.SHIPWRECK_TREASURE.key to Chances.SUPER_LOW,
        LootTables.SHIPWRECK_MAP.key to Chances.SUPER_LOW,

        LootTables.IGLOO_CHEST.key to Chances.LOW,

        LootTables.UNDERWATER_RUIN_SMALL.key to Chances.SUPER_LOW,
        LootTables.UNDERWATER_RUIN_BIG.key to Chances.SUPER_LOW,

        LootTables.END_CITY_TREASURE.key to Chances.MEDIUM
    )
    val LOOT_TABLE = JewelChestLootTable()

    val MAP_CHEST_UUID = NamespacedKey("questsmp", "map_chest/uuid")
    val MAP_CHEST_GENERATED = NamespacedKey("questsmp", "map_chest/generated")

    fun generate(player: Player): ItemStack {
        val uuid = UUID.randomUUID()

        val pos = generateChest(uuid, player.world, player.location)

        // Create item
        val nmsLevel = (player.world as CraftWorld).handle
        val nmsStack = MapItem.create(nmsLevel, pos.x, pos.z, 2, true, true)
        MapItem.renderBiomePreviewMap(nmsLevel, nmsStack)
        MapItemSavedData.addTargetDecoration(nmsStack, pos, "+", MapDecoration.Type.TARGET_X)

        val stack = nmsStack.bukkitStack
        val meta = stack.itemMeta
        meta.setCustomModelData(15)

        val container = meta.persistentDataContainer
        container.set(MAP_ID_KEY, PersistentDataType.STRING, uuid.toString())

        stack.itemMeta = meta

        return stack
    }

    fun generateChest(uuid: UUID, world: World, loc: Location): BlockPos {
        // mix the seed to avoid accidental collisions
        val salt = Random.nextLong()
        val seed = ((world.seed shl 7 or loc.blockX.toLong() + loc.blockZ or loc.blockY.toLong()) xor loc.yaw.toLong()) + salt
        val random = Random(seed)

        OnTheQuest.plugin.logger.info("Generated new map $uuid with seed $seed (${world.seed} ${loc.blockX} ${loc.blockY} ${loc.blockZ} ${loc.yaw.toLong()} $salt)")

        val x = random.nextInt(loc.blockX - 2048, loc.blockX + 2048)
        val z = random.nextInt(loc.blockZ - 2048, loc.blockZ + 2048)

        val blockPos = BlockPos(x, 100, z)

        addChestToGenerationQueue(world, blockPos, uuid)

        return blockPos
    }

    fun getMapShard(count: Int = 1): ItemStack {
        val itemStack = ItemStack(Material.MAP, count)

        val meta = itemStack.itemMeta
        meta.setCustomModelData(15)
        meta.displayName(
            Component
                .text("Map Shard")
                .color(TextColor.color(Rarity.RARE.color.color!!))
                .decoration(TextDecoration.ITALIC, false)
        )
        itemStack.itemMeta = meta

        return itemStack
    }

    private data class QueuedChest(
        val world: World,
        val pos: BlockPos,
        val uuid: UUID
    )

    private val chestGenerationQueue = ConcurrentLinkedQueue<QueuedChest>()

    fun init() {
        if (!OnTheQuest.plugin.dataFolder.exists())
            OnTheQuest.plugin.dataFolder.mkdirs()

        val file = File(OnTheQuest.plugin.dataFolder, "chest_generation.json")
        if (file.exists()) {
            val json = JsonParser.parseReader(file.reader()).asJsonArray

            json.forEach {
                val obj = it.asJsonObject

                val worldUid = UUID.fromString(obj.get("world").asString)
                val x = obj.get("x").asInt
                val z = obj.get("z").asInt
                val uuid = UUID.fromString(obj.get("uuid").asString)

                val world = Bukkit.getWorld(worldUid) ?: return@forEach
                chestGenerationQueue.add(QueuedChest(world, BlockPos(x, 100, z), uuid))
            }
        }
    }

    private fun save() {
        if (!OnTheQuest.plugin.dataFolder.exists())
            OnTheQuest.plugin.dataFolder.mkdirs()

        val file = File(OnTheQuest.plugin.dataFolder, "chest_generation.json")

        if (!file.exists())
            file.createNewFile()

        val jsonArray = JsonArray()
        chestGenerationQueue.forEach {
            val jsonObject = JsonObject()
            jsonObject.addProperty("world", it.world.uid.toString())
            jsonObject.addProperty("x", it.pos.x)
            jsonObject.addProperty("z", it.pos.z)
            jsonObject.addProperty("uuid", it.uuid.toString())

            jsonArray.add(jsonObject)
        }

        file.writeText(jsonArray.toString())
    }

    private fun addChestToGenerationQueue(world: World, pos: BlockPos, uuid: UUID) {
        val chunkX = pos.x / 16
        val chunkZ = pos.z / 16
        if (world.isChunkGenerated(chunkX, chunkZ) && world.isChunkLoaded(chunkX, chunkZ)) {
            val chunk = world.getChunkAt(chunkX, chunkZ)
            checkGenerationQueue(chunk)
        } else {
            chestGenerationQueue.add(QueuedChest(world, pos, uuid))
            save()
        }
    }

    fun checkGenerationQueue(chunk: Chunk) {
        OnTheQuest.plugin.server.scheduler.runTaskAsynchronously(OnTheQuest.plugin, Runnable {
            val chests = chestGenerationQueue.filter { it.world.uid == chunk.world.uid && it.pos.x / 16 == chunk.x && it.pos.z / 16 == chunk.z }

            if (chests.isEmpty())
                return@Runnable

            val chestPlacementQueue = mutableListOf<QueuedChest>()

            chests.forEach {
                chestGenerationQueue.remove(it)
                val mutablePos = it.pos.mutable()
                val x = it.pos.x
                val z = it.pos.z

                var foundY: Int? = null
                for (y in Random.nextInt(-53, 37)..42) {
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
                        foundY = y
                        break
                    }
                }

                if (foundY == null)
                    foundY = Random.nextInt(-53, 37)

                mutablePos.y = foundY

                OnTheQuest.plugin.logger.info("Generated chest ${it.uuid} at ${mutablePos.x} ${mutablePos.y} ${mutablePos.z}")
                chestPlacementQueue.add(QueuedChest(chunk.world, mutablePos, it.uuid))
            }

            OnTheQuest.plugin.server.scheduler.runTask(OnTheQuest.plugin, Runnable {
                chestPlacementQueue.forEach {
                    val block = it.world.getBlockAt(it.pos.x, it.pos.y, it.pos.z)

                    block.type = Material.CHEST
                    val state = block.getState(false) as Chest
                    state.persistentDataContainer.set(MAP_CHEST_UUID, PersistentDataType.STRING, it.uuid.toString())

                    OnTheQuest.plugin.logger.info("Placed chest ${it.uuid} at ${it.pos.x} ${it.pos.y} ${it.pos.z}")
                }
            })

            save()
        })
    }
}