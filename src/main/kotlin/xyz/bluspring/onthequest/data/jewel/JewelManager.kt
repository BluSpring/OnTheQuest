package xyz.bluspring.onthequest.data.jewel

import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.bluspring.onthequest.data.QuestRegistries
import java.util.*

object JewelManager {
    val JEWEL_KEY = NamespacedKey("questsmp", "jewel_type")
    val JEWEL_LEVEL_KEY = NamespacedKey("questsmp", "jewel_level")

    private val random = RandomSource.create()

    // these are just caches. they don't need to be saved.
    private val playerJewels = mutableMapOf<UUID, Jewel>()
    private val playerLevels = mutableMapOf<UUID, Int>()

    fun isJewel(itemStack: ItemStack): Boolean {
        return itemStack.hasItemMeta() && itemStack.itemMeta.persistentDataContainer.has(Jewel.JEWEL_TYPE_KEY)
    }

    fun resetAbilityEffects(player: Player, jewel: Jewel) {
        jewel.getAbilitiesInLevelRange(jewel.maxLevel).forEach {
            it.resetEffects(player)
        }
    }

    fun replaceOldJewel(player: Player, jewel: Jewel, resetEffects: Boolean = false) {
        val jewelSlots = mutableListOf<ItemStack>()
        val level = getOrCreateLevel(player)

        if (resetEffects) {
            val oldJewel = getOrCreateJewel(player)
            resetAbilityEffects(player, oldJewel)
        }

        player.inventory.contents.forEachIndexed { index, itemStack ->
            if (itemStack == null)
                return@forEachIndexed

            if (isJewel(itemStack)) {
                jewelSlots.add(itemStack)
            }
        }

        if (isJewel(player.inventory.itemInOffHand))
            jewelSlots.add(player.inventory.itemInOffHand)

        player.inventory.removeItem(*jewelSlots.toTypedArray())
        player.inventory.addItem(jewel.getItem(level).bukkitStack)
    }

    fun setJewel(player: Player, jewel: Jewel) {
        replaceOldJewel(player, jewel)

        player.persistentDataContainer.set(JEWEL_KEY, PersistentDataType.STRING, jewel.id.toString())
        playerJewels[player.uniqueId] = jewel
    }

    fun getOrCreateLevel(player: Player): Int {
        if (playerLevels.containsKey(player.uniqueId))
            return playerLevels[player.uniqueId]!!

        if (player.persistentDataContainer.has(JEWEL_LEVEL_KEY)) {
            val currentLevel = player.persistentDataContainer.get(JEWEL_LEVEL_KEY, PersistentDataType.INTEGER)!!

            playerLevels[player.uniqueId] = currentLevel
            player.persistentDataContainer.set(JEWEL_LEVEL_KEY, PersistentDataType.INTEGER, currentLevel)
            return currentLevel
        }

        return 0
    }

    fun addToLevel(player: Player, amount: Int): Int {
        val jewel = getOrCreateJewel(player)
        val currentLevel = getOrCreateLevel(player)
        val new = currentLevel + amount

        playerLevels[player.uniqueId] = new
        player.persistentDataContainer.set(JEWEL_LEVEL_KEY, PersistentDataType.INTEGER, new)
        replaceOldJewel(player, jewel, true)

        return new
    }

    fun getOrCreateJewel(player: Player): Jewel {
        if (playerJewels.containsKey(player.uniqueId))
            return playerJewels[player.uniqueId]!!

        if (player.persistentDataContainer.has(JEWEL_KEY)) {
            val currentJewelId = player.persistentDataContainer.get(JEWEL_KEY, PersistentDataType.STRING)!!
            val currentJewel = QuestRegistries.JEWEL.get(ResourceLocation.tryParse(currentJewelId)) ?: Jewel.EMPTY

            setJewel(player, currentJewel)
            return currentJewel
        }

        val randomJewel = QuestRegistries.JEWEL.getRandom(random).get().value()
        setJewel(player, randomJewel)

        return randomJewel
    }
}