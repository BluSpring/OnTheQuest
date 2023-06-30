package xyz.bluspring.onthequest.data.quests.criterion

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer


class PlayerOpenNewLootChestTrigger : SimpleCriterionTrigger<PlayerOpenNewLootChestTrigger.TriggerInstance>() {
    companion object {
        val ID = ResourceLocation("questsmp", "player_open_new_loot_chest")
    }

    class TriggerInstance(player: EntityPredicate.Composite, val lootTables: List<ResourceLocation>) : AbstractCriterionTriggerInstance(ID, player) {
        override fun getCriterion(): ResourceLocation {
            return ID
        }

        override fun serializeToJson(predicateSerializer: SerializationContext): JsonObject {
            val json = super.serializeToJson(predicateSerializer)
            return json.apply {
                val array = JsonArray()
                lootTables.forEach {
                    array.add(it.toString())
                }
                this.add("loot_tables", array)
            }
        }
    }

    fun trigger(player: ServerPlayer, lootTable: ResourceLocation) {
        this.trigger(player) {
            it.lootTables.contains(lootTable)
        }
    }

    override fun getId(): ResourceLocation {
        return ID
    }

    override fun createInstance(
        obj: JsonObject,
        playerPredicate: EntityPredicate.Composite,
        predicateDeserializer: DeserializationContext
    ): TriggerInstance {
        val array = obj.getAsJsonArray("loot_tables")
        val lootTables = mutableListOf<ResourceLocation>()

        array.forEach {
            lootTables.add(ResourceLocation(it.asString))
        }

        return TriggerInstance(playerPredicate, lootTables)
    }
}