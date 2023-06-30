package xyz.bluspring.onthequest.data.ability.custom

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.custom.earth.GaiasBlessingAbility

// These abilities cannot be made data-driven, at least not easily, and so is made a custom ability.
// Custom abilities are completely free to control whatever they want.
object CustomAbilities {
    val GAIAS_BLESSING_SHOW_MARKED = register("earth/gaias_blessing/show_marked", GaiasBlessingAbility.ShowMarked())
    val GAIAS_BLESSING_TOGGLE_MARK = register("earth/gaias_blessing/toggle_mark", GaiasBlessingAbility.ToggleMark())
    val GAIAS_BLESSING_HEAL_AREA = register("earth/gaias_blessing/heal_radius", GaiasBlessingAbility.HealArea())

    fun init() {}

    private fun register(key: String, type: Ability): Ability {
        return Registry.register(QuestRegistries.ABILITY, ResourceLocation("questsmp", key), type)
    }
}