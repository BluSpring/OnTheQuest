package xyz.bluspring.onthequest.data.jewel

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import xyz.bluspring.onthequest.data.util.predicates.RangePredicate

data class Jewel(
    val id: ResourceLocation,
    val item: ItemStack,
    val abilities: List<JewelAbility>
) {
    data class JewelAbility(
        val level: RangePredicate.IntPredicate,
        val passive: List<ResourceLocation>,
        val active: List<ResourceLocation>
    ) {
        companion object {
            fun deserialize(json: JsonObject): JewelAbility {
                return JewelAbility(
                    if (json.has("level"))
                        RangePredicate.IntPredicate.deserialize(json.getAsJsonObject("level"))
                    else
                        RangePredicate.IntPredicate(Int.MIN_VALUE, Int.MAX_VALUE),
                    if (json.has("passive"))
                        mutableListOf<ResourceLocation>().apply {
                            json.getAsJsonArray("passive").forEach {
                                this.add(ResourceLocation.tryParse(it.asString)!!)
                            }
                        }
                    else
                        listOf(),
                    if (json.has("active"))
                        mutableListOf<ResourceLocation>().apply {
                            json.getAsJsonArray("active").forEach {
                                this.add(ResourceLocation.tryParse(it.asString)!!)
                            }
                        }
                    else
                        listOf()
                )
            }
        }
    }

    companion object {
        val EMPTY = Jewel(ResourceLocation("questsmp", "empty"), ItemStack.EMPTY, listOf())

        fun deserialize(json: JsonObject, id: ResourceLocation): Jewel {
            val modelData = json.getAsJsonObject("model")

            return Jewel(
                id,
                ItemStack(
                    Registry.ITEM.get(ResourceLocation.tryParse(modelData.get("item").asString)),
                    1
                ),
                mutableListOf<JewelAbility>().apply {
                    json.getAsJsonArray("abilities").forEach {
                        this.add(JewelAbility.deserialize(it.asJsonObject))
                    }
                }
            )
        }
    }
}