package xyz.bluspring.onthequest.data.quests

import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.advancements.Advancement
import net.minecraft.resources.ResourceLocation
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
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
                }
            }
        }
    }

    fun isAdvancementAQuest(advancement: Advancement, jewel: Jewel): Boolean {
        return this.questsByAll.contains(advancement.id) || this.questsByJewel[jewel.id]?.contains(advancement.id) == true
    }

    fun getQuestCard(count: Int): ItemStack {
        val stack = ItemStack(Material.PAPER, count)

        val meta = stack.itemMeta

        meta.setCustomModelData(1)
        meta.displayName(Component.text("Quest Card").decoration(TextDecoration.ITALIC, false))

        stack.itemMeta = meta

        return stack
    }

    fun isQuestCard(stack: ItemStack): Boolean {
        return stack.hasItemMeta() && stack.type == Material.PAPER && stack.itemMeta.hasCustomModelData() && stack.itemMeta.customModelData == 1
    }

    fun getAllPossibleQuests(jewel: Jewel): List<ResourceLocation> {
        val quests = mutableListOf<ResourceLocation>()

        if (questsByJewel.contains(jewel.id)) {
            quests.addAll(questsByJewel[jewel.id]!!)
        }

        quests.addAll(questsByAll)

        return quests
    }
}