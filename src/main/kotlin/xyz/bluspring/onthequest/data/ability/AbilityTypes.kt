package xyz.bluspring.onthequest.data.ability

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.effect.EffectAddAbility
import xyz.bluspring.onthequest.data.ability.loot.LootHarvestAbility
import xyz.bluspring.onthequest.data.ability.loot.LootRecipeAbility

object AbilityTypes {
    val EMPTY = register("empty", EmptyAbilityType())

    val LOOT_HARVEST = register("loot/harvest", LootHarvestAbility.Type())
    val LOOT_RECIPE = register("loot/recipe", LootRecipeAbility.Type())

    val EFFECT_ADD = register("effects/add", EffectAddAbility.Type())

    fun init() {}

    private fun register(key: String, type: AbilityType): AbilityType {
        return Registry.register(QuestRegistries.ABILITY_TYPE, ResourceLocation("questsmp", key), type)
    }
}