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
import xyz.bluspring.onthequest.data.ability.AbilityTypes
import xyz.bluspring.onthequest.data.jewel.JewelManager
import xyz.bluspring.onthequest.data.particle.ParticleSpawnTypes
import xyz.bluspring.onthequest.data.quests.QuestCustomCriterias
import xyz.bluspring.onthequest.events.AbilityEventHandler
import xyz.bluspring.onthequest.events.JewelEventHandler
import xyz.bluspring.onthequest.events.QuestEventHandler
import xyz.bluspring.onthequest.events.QuestPackEventHandler
import java.io.File

class OnTheQuest : JavaPlugin() {
    override fun onLoad() {
        if (debug) {
            slF4JLogger.info("Debug mode has been enabled! If you are seeing this, the developer has likely completely forgotten to turn this off!")
        }

        plugin = this
        CommandAPI.onLoad(CommandAPIConfig())

        QuestCustomCriterias.init()
        ParticleSpawnTypes.init()
        AbilityTypes.init()

        QuestDatapackManager.reload()
    }

    fun getPluginFile(): File {
        return this.file
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)

        QuestDatapackManager.loadAllResources()

        this.server.pluginManager.registerEvents(JewelEventHandler(), this)
        this.server.pluginManager.registerEvents(QuestEventHandler(), this)
        this.server.pluginManager.registerEvents(QuestPackEventHandler(), this)
        this.server.pluginManager.registerEvents(AbilityEventHandler(), this)

        commandAPICommand("give-jewel") {
            withPermission("otq.admin")
            entitySelectorArgumentOnePlayer("player")
            namespacedKeyArgument("jewel_type") {
                replaceSuggestions(ArgumentSuggestions.strings(QuestRegistries.JEWEL.keySet().map { it.toString() }))
            }
            integerArgument("level", -2)
            playerExecutor { player, args ->
                val to = args[0] as Player
                val jewelId = args[1] as NamespacedKey
                val level = args[2] as Int

                if (!giveJewelItem(to, player, jewelId, level))
                    throw CommandAPI.failWithString("Invalid jewel type!")
            }
        }

        commandAPICommand("give-jewel") {
            withPermission("otq.admin")
            entitySelectorArgumentOnePlayer("player")
            namespacedKeyArgument("jewel_type") {
                replaceSuggestions(ArgumentSuggestions.strings(QuestRegistries.JEWEL.keySet().map { it.toString() }))
            }
            integerArgument("level", -2)
            integerArgument("count", 1)
            playerExecutor { player, args ->
                val to = args[0] as Player
                val jewelId = args[1] as NamespacedKey
                val level = args[2] as Int
                val count = args[3] as Int

                if (!giveJewelItem(to, player, jewelId, level, count))
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

                randomPlayer.world.dropItem(randomPlayer.location, randomJewel.getItem(0).asBukkitCopy())

                player.sendMessage("${randomPlayer.name} was randomly chosen to give the jewel type: ${randomJewel.id}")
            }
        }

        this.server.scheduler.runTaskTimer(this, Runnable {
            this.server.onlinePlayers.forEach {
                val jewel = JewelManager.getOrCreateJewel(it)
                jewel.displayCooldowns(it)
            }
        }, 0L, 20L)
    }

    private fun giveJewelItem(to: Player, player: Player, key: NamespacedKey, level: Int, count: Int = 1): Boolean {
        val jewelType = QuestRegistries.JEWEL.get(ResourceLocation(key.namespace, key.key)) ?: return false

        JewelManager.setJewel(to, jewelType)

        player.sendMessage(
            Component.text("Successfully gave ")
                .append(to.name())
                .append(Component.text(" ${count}x "))
                .append(Component.translatable("jewel.${key.namespace}.${key.key}"))
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