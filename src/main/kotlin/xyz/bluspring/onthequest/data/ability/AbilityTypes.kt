package xyz.bluspring.onthequest.data.ability

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.effect.EffectAddAbility
import xyz.bluspring.onthequest.data.ability.effect.EffectClearAbility
import xyz.bluspring.onthequest.data.ability.effect.EffectDisableAbility
import xyz.bluspring.onthequest.data.ability.loot.LootHarvestAbility
import xyz.bluspring.onthequest.data.ability.loot.LootRecipeAbility
import xyz.bluspring.onthequest.data.ability.meta.MultipleAbility
import xyz.bluspring.onthequest.data.ability.meta.RunCommandAbility

object AbilityTypes {
    val EMPTY = register("empty", EmptyAbilityType())

    val LOOT_HARVEST = register("loot/harvest", LootHarvestAbility.Type())
    val LOOT_RECIPE = register("loot/recipe", LootRecipeAbility.Type())

    val EFFECT_ADD = register("effects/add", EffectAddAbility.Type())
    val EFFECT_CLEAR = register("effects/clear", EffectClearAbility.Type())
    val EFFECT_DISABLE = register("effects/disable", EffectDisableAbility.Type())

    val MULTIPLE = register("multiple", MultipleAbility.Type())
    val RUN_COMMAND = register("run_command", RunCommandAbility.Type())

    fun init() {}

    private fun register(key: String, type: AbilityType): AbilityType {
        return Registry.register(QuestRegistries.ABILITY_TYPE, ResourceLocation("questsmp", key), type)
    }
}