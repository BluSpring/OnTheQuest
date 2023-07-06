package xyz.bluspring.onthequest.data.crafting

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.item.CustomItem
import xyz.bluspring.onthequest.data.item.CustomItemManager

object CraftingManager {
    private fun makeShapedRecipe(item: CustomItem, count: Int = 1): ShapedRecipe {
        return ShapedRecipe(NamespacedKey.fromString(QuestRegistries.CUSTOM_ITEM.getKey(item)!!.toString())!!, item.getItem(count).asBukkitMirror())
    }

    fun init() {
        /*
        Gold Block Diamond Gold Block
        Neth ingot Nether Star Neth ingot
        Gold Block Diamond Gold Block
         */

        // Revive
        run {
            val recipe = makeShapedRecipe(CustomItemManager.REVIVE)

            recipe.shape("GDG", "NSN", "GDG")
            recipe.setIngredient('G', Material.GOLD_BLOCK)
            recipe.setIngredient('D', Material.DIAMOND)
            recipe.setIngredient('S', Material.NETHER_STAR)
            recipe.setIngredient('N', Material.NETHERITE_INGOT)

            Bukkit.getServer().addRecipe(recipe)
        }

        /*
        Obsidian      | Diamond Block   | Obsidian
        Diamond Block | Netherite Ingot | Diamond Block
        Obsidian      | Diamond Block   | Obsidian
         */

        // Reload
        run {
            val recipe = makeShapedRecipe(CustomItemManager.JEWEL_RELOADER)

            recipe.shape("ODO", "DND", "ODO")
            recipe.setIngredient('O', Material.OBSIDIAN)
            recipe.setIngredient('D', Material.DIAMOND_BLOCK)
            recipe.setIngredient('N', Material.NETHERITE_INGOT)

            Bukkit.getServer().addRecipe(recipe)
        }
    }
}