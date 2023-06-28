package xyz.bluspring.onthequest.data.ability.loot

import com.google.gson.JsonObject
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockState
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockDropItemEvent
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityModifier
import xyz.bluspring.onthequest.data.ability.AbilityType

class LootHarvestAbility(
    cooldownTicks: Long,
    val applyTo: List<Block>,
    val modifier: AbilityModifier.IntModifier
) : Ability(cooldownTicks) {
    override fun <T : Event> canTriggerForEvent(player: Player, event: T): Boolean {
        return event is BlockDropItemEvent && super.canTriggerForEvent(player, event)
    }

    override fun <T : Event> triggerForEvent(player: Player, event: T): Boolean {
        if (event !is BlockDropItemEvent)
            return false

        val nmsBlockState = (event.blockState as CraftBlockState).handle
        if (!applyTo.contains(nmsBlockState.block))
            return false

        event.items.forEach {
            it.itemStack.amount = modifier.get(it.itemStack.amount)
        }

        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
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

            val modifier = if (data.has("modifier")) {
                AbilityModifier.IntModifier.parse(data.getAsJsonObject("modifier"))
            } else
                AbilityModifier.IntModifier.Multiply(1)

            return LootHarvestAbility(
                cooldownTicks,
                blocks,
                modifier
            ).apply {
                abilities.add(this)
            }
        }
    }
}