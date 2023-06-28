package xyz.bluspring.onthequest.data.jewel

import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.util.predicates.RangePredicate

data class Jewel(
    val id: ResourceLocation,
    val item: Item,
    val startingModelId: Int,
    val abilities: List<JewelAbility>,
    val color: TextColor
) {
    val maxLevel: Int
        get() {
            return abilities.size
        }
    val minLevel = -2

    fun getItem(level: Int): ItemStack {
        return ItemStack(item, 1).apply {
            val meta = this.bukkitStack.itemMeta

            meta.displayName(
                Component
                    .translatable("jewels.${id.namespace}.${id.path.replace("/", ".")}")
                    .color(color)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.setCustomModelData(Mth.clamp(startingModelId + level, minLevel, maxLevel))
            meta.persistentDataContainer.set(JEWEL_TYPE_KEY, PersistentDataType.STRING, id.toString())

            this.bukkitStack.itemMeta = meta
        }
    }

    data class JewelAbility(
        val level: RangePredicate.IntPredicate,
        val passive: List<Ability>,
        val active: List<Ability>
    ) {
        companion object {
            fun deserialize(json: JsonObject): JewelAbility {
                val range = if (json.has("level"))
                    RangePredicate.IntPredicate.deserialize(json.getAsJsonObject("level"))
                else
                    RangePredicate.IntPredicate(Int.MIN_VALUE, Int.MAX_VALUE)

                return JewelAbility(
                    range,
                    if (json.has("passive"))
                        mutableListOf<Ability>().apply {
                            json.getAsJsonArray("passive").forEach {
                                this.add(QuestRegistries.ABILITY.get(ResourceLocation.tryParse(it.asString)!!)!!)
                            }
                        }
                    else
                        listOf(),
                    if (json.has("active"))
                        mutableListOf<Ability>().apply {
                            json.getAsJsonArray("active").forEach {
                                this.add(QuestRegistries.ABILITY.get(ResourceLocation.tryParse(it.asString)!!)!!)
                            }
                        }
                    else
                        listOf()
                )
            }
        }
    }

    companion object {
        val JEWEL_TYPE_KEY = NamespacedKey("questsmp", "jewel_type")

        val EMPTY = Jewel(ResourceLocation("questsmp", "empty"), Items.AIR, 0, listOf(), NamedTextColor.WHITE)

        fun deserialize(json: JsonObject, id: ResourceLocation): Jewel {
            val modelData = json.getAsJsonObject("model")

            return Jewel(
                id,
                Registry.ITEM.get(ResourceLocation.tryParse(modelData.get("item").asString)),
                modelData.get("id").asInt,
                mutableListOf<JewelAbility>().apply {
                    json.getAsJsonArray("abilities").forEach {
                        this.add(JewelAbility.deserialize(it.asJsonObject))
                    }
                },
                if (json.has("color"))
                    TextColor.fromCSSHexString(json.get("color").asString)!!
                else
                    NamedTextColor.WHITE
            )
        }
    }
}