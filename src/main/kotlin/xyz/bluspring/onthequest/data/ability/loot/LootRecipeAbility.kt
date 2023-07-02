package xyz.bluspring.onthequest.data.ability.loot

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType
import java.util.concurrent.ConcurrentHashMap

class LootRecipeAbility(cooldownTicks: Long, val recipeType: RecipeType<*>) : Ability(cooldownTicks) {
    private val recipeCache: ConcurrentHashMap<Material, Material> = ConcurrentHashMap()

    override fun canTrigger(player: Player): Boolean {
        if (!player.persistentDataContainer.has(LOOT_RECIPE_ACTIVE))
            player.persistentDataContainer.set(LOOT_RECIPE_ACTIVE, PersistentDataType.BYTE, 1)

        return super.canTrigger(player) && player.persistentDataContainer.get(LOOT_RECIPE_ACTIVE, PersistentDataType.BYTE) == (1).toByte()
    }

    override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return (event is BlockDropItemEvent || event is EntityDeathEvent) && super.canTriggerForEvent(player, event)
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        if (event !is BlockDropItemEvent && event !is EntityDeathEvent)
            return false

        val recipes = (player.server as CraftServer).handle.server.recipeManager.recipes[recipeType] ?: return false

        return when (event) {
            is BlockDropItemEvent -> {
                event.items.forEach {
                    val stack = it.itemStack
                    if (!applyRecipeToItemStack(stack, recipes)) {
                        return@forEach
                    }

                    it.itemStack = stack
                }

                true
            }

            is EntityDeathEvent -> {
                if (event.entity is Player)
                    return false

                event.drops.forEach {
                    if (!applyRecipeToItemStack(it, recipes))
                        return@forEach
                }
                true
            }

            else -> false
        }
    }

    private fun applyRecipeToItemStack(stack: ItemStack, recipes: Map<ResourceLocation, Recipe<*>>): Boolean {
        if (!recipeCache.contains(stack.type)) {
            val recipe = recipes.values.firstOrNull { recipe ->
                !recipe.isIncomplete && recipe.ingredients.isNotEmpty() && recipe.ingredients.size == 1 && recipe.ingredients.any { ingredient ->
                    ingredient.items.any {
                        it.bukkitStack.type == stack.type
                    }
                }
            }

            if (recipe == null) {
                recipeCache[stack.type] = stack.type
                return false
            }

            recipeCache[stack.type] = recipe.resultItem.bukkitStack.type
            stack.type = recipe.resultItem.bukkitStack.type

            return true
        }

        stack.type = recipeCache[stack.type]!!
        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val recipeType = Registry.RECIPE_TYPE.get(ResourceLocation.tryParse(data.get("recipe_type").asString))!!

            return LootRecipeAbility(cooldownTicks, recipeType)
        }
    }

    companion object {
        val LOOT_RECIPE_ACTIVE = NamespacedKey("questsmp", "loot_recipe_active")
    }
}