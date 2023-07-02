package xyz.bluspring.onthequest.data.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration

enum class KeybindType(val key: String, val isLeftClick: Boolean, val isShift: Boolean) {
    NONE("none", false, false),
    PRIMARY_ABILITY("key.questsmp.primary_ability", true, false),
    SECONDARY_ABILITY("key.questsmp.secondary_ability", false, false),
    TERNARY_ABILITY("key.questsmp.ternary_ability", true, true),
    QUATERNARY_ABILITY("key.questsmp.quaternary_ability", false, true);

    fun getComponent(): Component {
        return Component.join(JoinConfiguration.separator(Component.text(" + ")),
            mutableListOf<Component>().apply {
                if (isNone())
                    this.add(Component.text("None"))

                if (isLeftClick)
                    this.add(Component.keybind(LEFT_CLICK_KEY))
                else
                    this.add(Component.keybind(RIGHT_CLICK_KEY))

                if (isShift)
                    this.add(Component.keybind(SHIFT_KEY))
            }
        )
    }

    fun isNone(): Boolean {
        return this == NONE
    }

    companion object {
        const val LEFT_CLICK_KEY = "key.attack"
        const val RIGHT_CLICK_KEY = "key.use"
        const val SHIFT_KEY = "key.sneak"

        fun get(isLeftClick: Boolean, isShift: Boolean): KeybindType {
            return KeybindType.values().first { it != NONE && it.isLeftClick == isLeftClick && it.isShift == isShift }
        }

        fun fromKey(key: String): KeybindType {
            return KeybindType.values().first { it.key == key }
        }
    }
}