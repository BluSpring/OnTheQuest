package xyz.bluspring.onthequest.data.quests

import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.advancements.CriterionTrigger
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.data.quests.criterion.PlayerOpenNewLootChestTrigger
import xyz.bluspring.onthequest.util.ReflectionHelper

object QuestCustomCriterias {
    private val CRITERIA_FIELD = CriteriaTriggers::class.java.getDeclaredField(
        ReflectionHelper.reflectionRemapper.remapFieldName(
            CriteriaTriggers::class.java, "CRITERIA"))

    private val criterias: HashMap<ResourceLocation, CriterionTrigger<*>>

    init {
        CRITERIA_FIELD.isAccessible = true
        criterias = CRITERIA_FIELD.get(null) as HashMap<ResourceLocation, CriterionTrigger<*>>
    }

    private fun <T : CriterionTrigger<*>> register(criteria: T): T {
        criterias[criteria.id] = criteria
        return criteria
    }

    fun init() {}

    val PLAYER_OPEN_NEW_LOOT_CHEST = register(PlayerOpenNewLootChestTrigger())
}