package xyz.bluspring.onthequest.generation

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import java.lang.IllegalStateException
import java.util.UUID
import kotlin.random.Random

object MapChestManager {
    fun generate(player: Player): ItemStack {
        val uuid = UUID.randomUUID()

        val pos = generateChest(uuid, player.world, player.location)

        // Create item
        val stack = Bukkit.createExplorerMap(pos.world, pos, StructureType.BURIED_TREASURE)
        val meta = stack.itemMeta
        meta.setCustomModelData(15)

        val container = meta.persistentDataContainer
        container.set(NamespacedKey("questsmp", "map_id"), PersistentDataType.STRING, uuid.toString())

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
            if (!chunk.isLoaded) {
                // If the chunk failed to load, let's just skip over it.
                if (!chunk.load(true))
                    continue
            }

            do {
                val y = random.nextInt(-43, 49)
                val block = chunk.getBlock(x, y, z)

                // check adjacent blocks
                if (
                    block.location.add(0.0, 1.0, 0.0).block.isSolid
                    && block.location.add(0.0, -1.0, 0.0).block.isSolid
                    && block.location.add(1.0, 0.0, 0.0).block.isSolid
                    && block.location.add(-1.0, 0.0, 0.0).block.isSolid
                    && block.location.add(0.0, 0.0, 1.0).block.isSolid
                    && block.location.add(0.0, 0.0, -1.0).block.isSolid
                ) {
                    foundPos = true

                    location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                }
            } while (!foundPos)
        } while (!foundPos)

        if (location == null)
            throw IllegalStateException("how in the world did this even happen")

        val block = location.block
        block.type = Material.CHEST
        block.setMetadata("questsmp_map_chest", MapChestMetadata(uuid))

        if (OnTheQuest.debug)
            Bukkit.broadcast(Component.text("[OnTheQuest DEBUG] New chest generated at ${location.blockX} ${location.blockY} ${location.blockZ}"))

        return location
    }
}