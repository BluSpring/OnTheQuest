package xyz.bluspring.onthequest

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIConfig
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import xyz.bluspring.onthequest.events.CustomMapEventHandler
import xyz.bluspring.onthequest.events.JewelCraftingEventHandler
import xyz.bluspring.onthequest.events.JewelEffectEventHandler
import xyz.bluspring.onthequest.events.ModifiedLootTablesEventHandler
import xyz.bluspring.onthequest.generation.MapChestManager
import xyz.bluspring.onthequest.jewel.JewelType
import xyz.bluspring.onthequest.jewel.Jewels
import xyz.bluspring.onthequest.util.MappedRegistry

class OnTheQuestBukkit : JavaPlugin() {
    override fun onLoad() {
        plugin = this
        CommandAPI.onLoad(CommandAPIConfig())
        MapChestManager.init()
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)

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

        commandAPICommand("jewel-random") {
            withPermission("otq.admin")
            playerExecutor { player, _ ->
                val randomPlayer = Bukkit.getOnlinePlayers().random()
                val randomJewel = Jewels.REGISTRY.toList().random()

                //JewelEffectEventHandler.applyTemporaryJewel(randomPlayer, randomJewel)

                randomPlayer.world.dropItem(randomPlayer.location, randomJewel.getItem(1))

                player.sendMessage("${randomPlayer.name} was randomly chosen to give the jewel type: ${randomJewel.key}")
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

        run {
            val itemStack = Jewels.DRAGON.getItem(1)

            val dragonJewelRecipe = ShapedRecipe(NamespacedKey("questsmp", "dragon_jewel"), itemStack)
                .apply {
                    shape("GDG", "DED", "GDG")
                    setIngredient('G', Material.GOLD_BLOCK)
                    setIngredient('D', Material.DIAMOND_BLOCK)
                    setIngredient('E', Material.DRAGON_EGG)
                }

            this.server.addRecipe(dragonJewelRecipe)
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
        lateinit var plugin: OnTheQuestBukkit
        val debug = System.getProperty("otq.debugMode", "false") == "true"
    }
}