package xyz.bluspring.onthequest.data.quests

import com.google.gson.JsonObject
import net.minecraft.advancements.Advancement
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.jewel.Jewel

object QuestManager {
    // Jewel ID : List<Advancement ID>
    val questsByJewel = mutableMapOf<ResourceLocation, MutableList<ResourceLocation>>()
    val questsByAll = mutableListOf<ResourceLocation>()

    fun parseFromJson(json: JsonObject) {
        json.keySet().forEach {
            if (it == "*") {
                json.getAsJsonArray(it).forEach { id ->
                    questsByAll.add(ResourceLocation.tryParse(id.asString)!!)
                }
            } else {
                val jewelId = ResourceLocation.tryParse(it)!!
                json.getAsJsonArray(it).forEach { id ->
                    if (!questsByJewel.contains(jewelId))
                        questsByJewel[jewelId] = mutableListOf()

                    questsByJewel[jewelId]?.add(ResourceLocation.tryParse(id.asString)!!)
                    OnTheQuest.logger.info("")
                }
            }
        }
    }

    fun isAdvancementAQuest(advancement: Advancement, jewel: Jewel): Boolean {
        return this.questsByAll.contains(advancement.id) || this.questsByJewel[jewel.id]?.contains(advancement.id) == true
    }
}