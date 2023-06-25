package xyz.bluspring.onthequest

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIConfig
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import xyz.bluspring.onthequest.data.QuestDatapackManager
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.events.QuestPackEventHandler
import java.io.File

class OnTheQuest : JavaPlugin() {
    override fun onLoad() {
        if (debug) {
            slF4JLogger.info("Debug mode has been enabled! If you are seeing this, the developer has likely completely forgotten to turn this off!")
        }

        plugin = this
        CommandAPI.onLoad(CommandAPIConfig())

        QuestDatapackManager.reload()
    }

    fun getPluginFile(): File {
        return this.file
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)

        this.server.pluginManager.registerEvents(QuestPackEventHandler(), this)

        commandAPICommand("give-jewel") {
            withPermission("otq.admin")
            entitySelectorArgumentOnePlayer("player")
            namespacedKeyArgument("jewel_type") {
                replaceSuggestions(ArgumentSuggestions.strings(QuestRegistries.JEWEL.keySet().map { it.toString() }))
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
                replaceSuggestions(ArgumentSuggestions.strings(QuestRegistries.JEWEL.keySet().map { it.toString() }))
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

        val randomSource = RandomSource.create()

        commandAPICommand("jewel-random") {
            withPermission("otq.admin")
            playerExecutor { player, _ ->
                val randomPlayer = Bukkit.getOnlinePlayers().random()
                val randomJewel = QuestRegistries.JEWEL.getRandom(randomSource).get().value()

                //JewelEffectEventHandler.applyTemporaryJewel(randomPlayer, randomJewel)

                randomPlayer.world.dropItem(randomPlayer.location, randomJewel.item.asBukkitCopy())

                player.sendMessage("${randomPlayer.name} was randomly chosen to give the jewel type: ${randomJewel.id}")
            }
        }
    }

    private fun giveJewelItem(to: Player, player: Player, key: NamespacedKey, count: Int = 1): Boolean {
        val jewelType = QuestRegistries.JEWEL.get(ResourceLocation(key.namespace, key.key)) ?: return false

        to.inventory.addItem(jewelType.item.asBukkitCopy().asQuantity(count))

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
        val debug = true

        val logger: Logger
            get() = plugin.slF4JLogger
    }
}