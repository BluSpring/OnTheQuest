package xyz.bluspring.onthequest.data.ability.loot

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityDropItemEvent
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
        return (event is BlockDropItemEvent || event is EntityDropItemEvent) && super.canTriggerForEvent(player, event)
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        if (event !is BlockDropItemEvent && event !is EntityDropItemEvent)
            return false

        val recipes = (player.server as CraftServer).handle.server.recipeManager.recipes[recipeType] ?: return false

        return if (event is BlockDropItemEvent) {
            event.items.forEach {
                if (!recipeCache.contains(it.itemStack.type)) {
                    val recipe = recipes.values.firstOrNull { recipe ->
                        !recipe.isIncomplete && recipe.ingredients.isNotEmpty() && recipe.ingredients.size == 1 && recipe.ingredients.any { ingredient ->
                            ingredient.items.any { stack ->
                                stack.bukkitStack.type == it.itemStack.type
                            }
                        }
                    }

                    if (recipe == null) {
                        recipeCache[it.itemStack.type] = it.itemStack.type
                        return@forEach
                    }

                    recipeCache[it.itemStack.type] = recipe.resultItem.bukkitStack.type
                    it.itemStack.type = recipe.resultItem.bukkitStack.type

                    return@forEach
                }

                it.itemStack.type = recipeCache[it.itemStack.type]!!
            }

            true
        } else if (event is EntityDropItemEvent) {
            val it = event.itemDrop

            if (!recipeCache.contains(it.itemStack.type)) {
                val recipe = recipes.values.firstOrNull { recipe ->
                    !recipe.isIncomplete && recipe.ingredients.isNotEmpty() && recipe.ingredients.size == 1 && recipe.ingredients.any { ingredient ->
                        ingredient.items.any { stack ->
                            stack.bukkitStack.type == it.itemStack.type
                        }
                    }
                }

                if (recipe == null) {
                    recipeCache[it.itemStack.type] = it.itemStack.type
                    return false
                }

                recipeCache[it.itemStack.type] = recipe.resultItem.bukkitStack.type
                it.itemStack.type = recipe.resultItem.bukkitStack.type

                return false
            }

            it.itemStack.type = recipeCache[it.itemStack.type]!!
            true
        } else false
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