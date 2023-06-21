package xyz.bluspring.onthequest.data.ability.loot

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityModifier
import xyz.bluspring.onthequest.data.ability.AbilityType

class LootHarvestAbility(
    val applyTo: List<Block>,
    val modifier: AbilityModifier.IntModifier
) : Ability() {

    class Type : AbilityType() {
        override fun create(data: JsonObject): Ability {
            val blocks = mutableListOf<Block>()

            val blockIds = data.getAsJsonArray("apply")
            blockIds.forEach {
                val isTag = it.asString.startsWith("#")
                val id = ResourceLocation.tryParse(it.asString.removePrefix("#"))

                if (id == null) {
                    OnTheQuest.logger.error("Failed to parse resource location \"${it.asString}\"")
                    return@forEach
                }

                if (isTag) {
                    val tag = TagKey.create(Registry.BLOCK_REGISTRY, id)

                    Registry.BLOCK.getTagOrEmpty(tag).forEach { block ->
                        blocks.add(block.value())
                    }
                }
            }

            return LootHarvestAbility(
                blocks,
                AbilityModifier.IntModifier.parse(data.getAsJsonObject("modifier"))
            )
        }
    }
}