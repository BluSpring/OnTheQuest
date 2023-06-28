package xyz.bluspring.onthequest.data.ability.meta

import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityType

class RunCommandAbility(cooldownTicks: Long, val command: String) : Ability(cooldownTicks) {
    override fun trigger(player: Player, location: Location?): Boolean {
        (Bukkit.getServer() as CraftServer).handle.server.commands.performPrefixedCommand((player as CraftPlayer).handle.createCommandSourceStack(), command)
        return true
    }

    class Type : AbilityType() {
        override fun create(data: JsonObject, cooldownTicks: Long): Ability {
            val command = data.get("command").asString

            return RunCommandAbility(cooldownTicks, command)
        }
    }
}