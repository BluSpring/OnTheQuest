package xyz.bluspring.onthequest.data.util

enum class KeybindType(val key: String, val isLeftClick: Boolean, val isShift: Boolean) {
    NONE("none", false, false),
    PRIMARY_ABILITY("key.questsmp.primary_ability", true, false),
    SECONDARY_ABILITY("key.questsmp.secondary_ability", false, false),
    TERNARY_ABILITY("key.questsmp.ternary_ability", true, true),
    QUATERNARY_ABILITY("key.questsmp.quaternary_ability", false, true);

    fun isNone(): Boolean {
        return this == NONE
    }

    companion object {
        fun get(isLeftClick: Boolean, isShift: Boolean): KeybindType {
            return KeybindType.values().first { it != NONE && it.isLeftClick == isLeftClick && it.isShift == isShift }
        }

        fun fromKey(key: String): KeybindType {
            return KeybindType.values().first { it.key == key }
        }
    }
}