package xyz.bluspring.onthequest.generation

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import xyz.bluspring.onthequest.jewel.JewelType
import xyz.bluspring.onthequest.jewel.Jewels
import java.util.*
import kotlin.random.asKotlinRandom

class JewelChestLootTable : LootTable {
    companion object {
        private val materials = listOf(Material.OBSIDIAN, Material.DIAMOND, Material.GOLD_INGOT, Material.GOLD_NUGGET)
    }

    override fun getKey(): NamespacedKey {
        return NamespacedKey("questsmp", "jewel_chest")
    }

    override fun populateLoot(random: Random?, context: LootContext): MutableCollection<ItemStack> {
        val randomGen = random?.asKotlinRandom() ?: kotlin.random.Random.Default

        val items = mutableListOf<ItemStack>()

        // Get the jewel
        run {
            if (randomGen.nextInt(5) == 0) {
                val chosen = mutableSetOf<JewelType>()
                for (jewelType in Jewels.REGISTRY) {
                    // Literally impossible
                    if (jewelType.probability == 0.0)
                        continue

                    // Invert
                    val probability = (1.0 / jewelType.probability).toInt()

                    if (randomGen.nextInt(probability) == 0)
                        chosen.add(jewelType)
                }

                // Only add one
                if (chosen.isNotEmpty())
                    items.add(chosen.random(randomGen).getItem(1))
            }
        }

        // Fill the chest with obsidian, diamonds and/or gold.
        run {
            for (i in 0..randomGen.nextInt(4, 18)) {
                val count = randomGen.nextInt(0, 9)

                items.add(ItemStack(materials.random(randomGen), count))
            }
        }

        return items
    }

    override fun fillInventory(inventory: Inventory, random: Random?, context: LootContext) {
        val items = populateLoot(random, context)
        val randomGen = random?.asKotlinRandom() ?: kotlin.random.Random.Default

        items.forEach {
            var slot: Int
            do {
                slot = randomGen.nextInt(inventory.size)
            } while (inventory.getItem(slot) != null)

            inventory.setItem(slot, it)
        }
    }
}