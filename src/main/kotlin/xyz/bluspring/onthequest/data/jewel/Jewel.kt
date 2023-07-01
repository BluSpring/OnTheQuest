package xyz.bluspring.onthequest.data.jewel

import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
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
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.util.CooldownUtil
import xyz.bluspring.onthequest.data.util.KeybindType
import xyz.bluspring.onthequest.data.util.predicates.RangePredicate

data class Jewel(
    val id: ResourceLocation,
    val item: Item,
    val startingModelId: Int,
    val abilities: List<JewelAbility>,
    val color: TextColor,
    val char: Char
) {
    val maxLevel: Int
        get() {
            return abilities.size
        }
    val minLevel = -2

    val translationKey = "jewels.${id.namespace}.${id.path}"

    fun displayInfo(player: Player) {
        val level = JewelManager.getOrCreateLevel(player)

        /*
        === X Jewel ===
        --[  Lv. 1  ]--
        Ability 1
        > Description stuff

        Ability 2 - (Shift + Right Click)
        > Description stuff

        --[  Lv. 2  ]--
         */

        val texts = mutableListOf<Component>(
            Component.text("=== ")
                .append(Component.translatable(this.translationKey).color(this.color).decoration(TextDecoration.BOLD, true))
                .append(Component.text(" ==="))
        )

        this.abilities.sortedBy { it.level.min }.forEach { abilityList ->
            texts.add(
                Component.text("--[  ")
                    .append(
                        Component.text("Lv. ${abilityList.level.min}")
                            .color(
                                if (abilityList.level.isInRange(level))
                                    NamedTextColor.GREEN
                                else
                                    NamedTextColor.RED
                            )
                    )
                    .append(Component.text("  ]--"))
            )

            abilityList.passive.forEach { ability ->
                val abilityId = QuestRegistries.ABILITY.getKey(ability)!!
                val key = "abilities.${abilityId.namespace}.${abilityId.path.replace("/", ".")}"
                texts.add(
                    Component.translatable(key)
                        .color(NamedTextColor.DARK_AQUA)
                        .apply {
                            if (ability.keybindType != KeybindType.NONE) {
                                this.append(
                                    Component.text(" - ")
                                        .color(NamedTextColor.WHITE)
                                        .append(Component.text("(").color(NamedTextColor.GOLD))
                                        .append(ability.keybindType.getComponent().color(NamedTextColor.GOLD))
                                        .append(Component.text(")").color(NamedTextColor.GOLD))
                                )
                            }
                        }
                )

                texts.add(
                    Component.text("> ")
                        .append(Component.translatable("$key.description"))
                )

                texts.add(Component.text(""))
            }

            abilityList.active.forEach { ability ->
                val abilityId = QuestRegistries.ABILITY.getKey(ability)!!
                val key = "abilities.${abilityId.namespace}.${abilityId.path.replace("/", ".")}"
                texts.add(
                    Component.translatable(key)
                        .color(NamedTextColor.DARK_GREEN)
                        .apply {
                            if (ability.keybindType != KeybindType.NONE) {
                                this.append(
                                    Component.text(" - ")
                                        .color(NamedTextColor.WHITE)
                                        .append(Component.text("(").color(NamedTextColor.GOLD))
                                        .append(ability.keybindType.getComponent().color(NamedTextColor.GOLD))
                                        .append(Component.text(")").color(NamedTextColor.GOLD))
                                )
                            }
                        }
                )

                texts.add(
                    Component.text("> ")
                        .append(Component.translatable("$key.description"))
                )

                texts.add(Component.text(""))
            }
        }

        player.sendMessage(Component.join(JoinConfiguration.newlines(), texts))
    }

    fun displayCooldowns(player: Player) {
        val text = mutableListOf<Component>()

        var index = 0
        abilities.forEach {
            it.active.forEach active@{ ability ->
                index++
                val cooldown = ability.getCooldown(player)

                if (cooldown == 0L)
                    return@active

                text.add(
                    Component.text("$char")
                        .append(Component.text(CooldownUtil.getSubscriptOfNumber(index)))
                        .append(Component.text("  ${CooldownUtil.getTimeString(cooldown * 50)}"))
                )
            }

            it.passive.forEach active@{ ability ->
                index++
                val cooldown = ability.getCooldown(player)

                if (cooldown == 0L)
                    return@active

                text.add(
                    Component.text("$char")
                        .append(Component.text(CooldownUtil.getSubscriptOfNumber(index)))
                        .append(Component.text("  ${CooldownUtil.getTimeString(cooldown * 50)}"))
                )
            }
        }

        player.sendActionBar(Component.join(JoinConfiguration.separator(Component.text(" | ")), text))
    }

    fun getPassiveAbilitiesInLevelRange(level: Int): List<Ability> {
        val abilities = mutableListOf<Ability>()
        val jewelAbilities = this.abilities.filter { it.level.isInRange(level) }

        jewelAbilities.forEach { jewelAbility ->
            abilities.addAll(jewelAbility.passive)
        }

        return abilities
    }

    fun getActiveAbilitiesInLevelRange(level: Int): List<Ability> {
        val abilities = mutableListOf<Ability>()
        val jewelAbilities = this.abilities.filter { it.level.isInRange(level) }

        jewelAbilities.forEach { jewelAbility ->
            abilities.addAll(jewelAbility.active)
        }

        return abilities
    }

    fun getAbilitiesInLevelRange(level: Int): List<Ability> {
        val abilities = mutableListOf<Ability>()
        val jewelAbilities = this.abilities.filter { it.level.isInRange(level) }

        jewelAbilities.forEach { jewelAbility ->
            abilities.addAll(jewelAbility.active)
            abilities.addAll(jewelAbility.passive)
        }

        return abilities
    }

    fun isAbilityInLevelRange(level: Int, ability: Ability): Boolean {
        return getAbilitiesInLevelRange(level).contains(ability)
    }

    fun getItem(level: Int): ItemStack {
        return ItemStack(item, 1).apply {
            val meta = this.bukkitStack.itemMeta

            meta.displayName(
                Component
                    .translatable("jewels.${id.namespace}.${id.path.replace("/", ".")}")
                    .color(color)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" [Lv. $level]").color(NamedTextColor.GOLD))
            )
            meta.setCustomModelData(Mth.clamp(startingModelId + level, startingModelId + minLevel, startingModelId + maxLevel))
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
                                val id = ResourceLocation.tryParse(it.asString)!!

                                try {
                                    this.add(QuestRegistries.ABILITY.get(id)!!)
                                } catch (e: Exception) {
                                    OnTheQuest.logger.error("Failed to load passive ability $id")
                                    e.printStackTrace()
                                }
                            }
                        }
                    else
                        listOf(),
                    if (json.has("active"))
                        mutableListOf<Ability>().apply {
                            json.getAsJsonArray("active").forEach {
                                val id = ResourceLocation.tryParse(it.asString)!!

                                try {
                                    this.add(QuestRegistries.ABILITY.get(id)!!)
                                } catch (e: Exception) {
                                    OnTheQuest.logger.error("Failed to load active ability $id")
                                    e.printStackTrace()
                                }
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

        val EMPTY = Jewel(ResourceLocation("questsmp", "empty"), Items.AIR, 0, listOf(), NamedTextColor.WHITE, 'U')

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
                    NamedTextColor.WHITE,
                if (json.has("char"))
                    json.get("char").asCharacter
                else
                    'J'
            )
        }
    }
}