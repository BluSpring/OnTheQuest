package xyz.bluspring.onthequest.jewel

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import xyz.bluspring.onthequest.OnTheQuestBukkit

abstract class EventedJewelType(
    id: NamespacedKey,
    modelId: Int,
    slots: List<EquipmentSlot> = listOf(),
    materials: List<Material> = listOf(),
    effectsWhenHeld: Boolean = false,
    probability: Double = 0.0
) : JewelType(id, listOf(), modelId, slots, materials, effectsWhenHeld, probability), Listener {
    init {
        registerEvent()
    }

    private fun registerEvent() {
        OnTheQuestBukkit.plugin.server.pluginManager.registerEvents(this, OnTheQuestBukkit.plugin)
    }

    private val players = mutableSetOf<Player>()

    override fun apply(player: Player) {
        if (!players.contains(player))
            players.add(player)
    }

    override fun remove(player: Player) {
        players.remove(player)
    }

    protected fun isApplied(player: Player): Boolean {
        return players.contains(player)
    }
}