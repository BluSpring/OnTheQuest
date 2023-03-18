package xyz.bluspring.onthequest

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIConfig
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import xyz.bluspring.onthequest.events.*
import xyz.bluspring.onthequest.generation.MapChestManager
import xyz.bluspring.onthequest.jewel.JewelType
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.MappedRegistry

class OnTheQuest : JavaPlugin() {
    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIConfig())
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)

        plugin = this

        this.server.pluginManager.registerEvents(CustomMapEventHandler(), this)
        this.server.pluginManager.registerEvents(JewelEffectEventHandler(), this)
        this.server.pluginManager.registerEvents(JewelCraftingEventHandler(), this)
        this.server.pluginManager.registerEvents(ModifiedLootTablesEventHandler(), this)
        Jewels.init()

        commandAPICommand("give-jewel") {
            withPermission("otq.admin")
            entitySelectorArgumentOnePlayer("player")
            namespacedKeyArgument("jewel_type") {
                replaceSuggestions(ArgumentSuggestions.strings((Jewels.REGISTRY as MappedRegistry<JewelType>).keys().map { it.toString() }))
            }
            playerExecutor { player, args ->
                val to = args[0] as Player
                val jewelId = args[1] as NamespacedKey

                if (!giveJewelItem(to, player, jewelId))
                    throw CommandAPI.failWithString("Invalid jewel type!")
            }
        }

        commandAPICommand("give-jewel") {
            withPermission("otq.admin")
            entitySelectorArgumentOnePlayer("player")
            namespacedKeyArgument("jewel_type") {
                replaceSuggestions(ArgumentSuggestions.strings((Jewels.REGISTRY as MappedRegistry<JewelType>).keys().map { it.toString() }))
            }
            integerArgument("count", 1)
            playerExecutor { player, args ->
                val to = args[0] as Player
                val jewelId = args[1] as NamespacedKey
                val count = args[2] as Int

                if (!giveJewelItem(to, player, jewelId, count))
                    throw CommandAPI.failWithString("Invalid jewel type!")
            }
        }

        run {
            val itemStack = ItemStack(Material.MAP, 1).apply {
                val meta = this.itemMeta
                meta.setCustomModelData(16)
                this.itemMeta = meta
            }

            val mapShard = MapChestManager.getMapShard(1)

            val mapRecipe = ShapelessRecipe(NamespacedKey("questsmp", "map"), itemStack)
            mapRecipe.addIngredient(4, mapShard)

            this.server.addRecipe(mapRecipe)
        }
    }

    private fun giveJewelItem(to: Player, player: Player, key: NamespacedKey, count: Int = 1): Boolean {
        val jewelType = Jewels.REGISTRY.get(key) ?: return false

        to.inventory.addItem(jewelType.getItem(count))

        player.sendMessage(
            Component.text("Successfully gave ")
                .append(to.name())
                .append(Component.text(" ${count}x "))
                .append(Component.translatable("item.${key.namespace}.${key.key}"))
        )

        return true
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }

    companion object {
        lateinit var plugin: OnTheQuest
        val debug = System.getProperty("otq.debugMode", "false") == "true"
    }
}