package xyz.bluspring.onthequest.generation

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
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

        val x = random.nextInt(loc.blockX - 512, loc.blockX + 512)
        val z = random.nextInt(loc.blockZ - 512, loc.blockZ + 512)

        val y = random.nextInt(-53, 37)

        val block = world.getBlockAt(x, y, z)
        block.type = Material.CHEST
        block.setMetadata("questsmp_map_chest", MapChestMetadata(uuid))

        if (OnTheQuest.debug)
            Bukkit.broadcast(Component.text("[OnTheQuest DEBUG] New chest generated at $x $y $z"))

        return block.location
    }
}