package xyz.bluspring.onthequest.data.condition

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.condition.types.CompareCondition
import xyz.bluspring.onthequest.data.condition.types.SubmergedInCondition

object Conditions {
    val COMPARE = register("compare", CompareCondition.Type())
    val SUBMERGED_IN = register("submerged_in", SubmergedInCondition.Type())

    fun init() {}

    private fun register(key: String, type: Condition.Type): Condition.Type {
        return Registry.register(QuestRegistries.CONDITION, ResourceLocation("questsmp", key), type)
    }
}