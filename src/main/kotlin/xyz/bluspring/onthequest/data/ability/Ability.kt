package xyz.bluspring.onthequest.data.ability

import org.bukkit.entity.Player

abstract class Ability {
    open fun triggerCooldown(player: Player) {

    }


}
