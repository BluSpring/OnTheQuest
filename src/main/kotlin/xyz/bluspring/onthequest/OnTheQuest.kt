package xyz.bluspring.onthequest

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import xyz.bluspring.onthequest.data.QuestDatapackManager
import xyz.bluspring.onthequest.data.QuestRegistries
import xyz.bluspring.onthequest.data.ability.AbilityTypes
import xyz.bluspring.onthequest.data.condition.Conditions
import xyz.bluspring.onthequest.data.crafting.CraftingManager
import xyz.bluspring.onthequest.data.item.CustomItemManager
import xyz.bluspring.onthequest.data.jewel.JewelManager
import xyz.bluspring.onthequest.data.particle.ParticleSpawnTypes
import xyz.bluspring.onthequest.data.quests.QuestCustomCriterias
import xyz.bluspring.onthequest.events.*
import java.io.File

class OnTheQuest : JavaPlugin() {
    override fun onLoad() {
        if (debug) {
            slF4JLogger.info("Debug mode has been enabled! If you are seeing this, the developer has likely completely forgotten to turn this off!")
        }

        plugin = this
        CommandAPI.onLoad(CommandAPIBukkitConfig(this))

        Conditions.init()
        QuestCustomCriterias.init()
        ParticleSpawnTypes.init()
        AbilityTypes.init()
        CustomItemManager.init()

        QuestDatapackManager.reload()
    }

    fun getPluginFile(): File {
        return this.file
    }

    override fun onEnable() {
        CommandAPI.onEnable()

        QuestDatapackManager.loadAllResources()

        CraftingManager.init()
        this.server.pluginManager.registerEvents(JewelEventHandler(), this)
        this.server.pluginManager.registerEvents(QuestEventHandler(), this)
        this.server.pluginManager.registerEvents(QuestPackEventHandler(), this)
        this.server.pluginManager.registerEvents(AbilityEventHandler(), this)
        this.server.pluginManager.registerEvents(CustomItemEventHandler(), this)

        commandTree("quest") {
            withAliases("q")

            literalArgument("jewel") {
                withShortDescription("Lists information about your jewel.")

                playerExecutor { player, _ ->
                    val jewel = JewelManager.getOrCreateJewel(player)

                    jewel.displayInfo(player)
                }
            }

            literalArgument("list") {
                withShortDescription("All of your currently unlocked and incomplete quests.")

                playerExecutor { player, _ ->
                    val tags = player.scoreboardTags
                    val questTags = tags.filter { it.startsWith("otq_") }

                    val quests = questTags
                        .asSequence()
                        .map {
                            it.removePrefix("otq_").replace(".", ":").replace("-", "/")
                        }
                        .map {
                            NamespacedKey.fromString(it)
                        }
                        .map {
                            if (it == null)
                                return@map null
                            Bukkit.getServer().getAdvancement(it)
                        }
                        .filterNotNull()
                        .filter { !player.getAdvancementProgress(it).isDone }
                        .toList()

                    if (quests.isEmpty()) {
                        player.sendMessage("${ChatColor.RED}>> You currently do not have any quests to complete!")

                        return@playerExecutor
                    }

                    val texts = mutableListOf<Component>(
                        Component.text("Incomplete Quests")
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.UNDERLINED, true)
                    )

                    quests.forEach {
                        texts.add(Component.text("- ").append(it.displayName()))
                    }

                    player.sendMessage(Component.join(JoinConfiguration.newlines(), texts))
                }
            }

            literalArgument("admin") {
                withPermission("otq.admin")

                literalArgument("level") {
                    literalArgument("set") {
                        entitySelectorArgumentOnePlayer("player") {
                            integerArgument("level") {
                                anyExecutor { player, args ->
                                    val to = args[0] as Player
                                    val level = args[1] as Int

                                    val jewel = JewelManager.getOrCreateJewel(to)
                                    val current = JewelManager.getOrCreateLevel(to)
                                    JewelManager.addToLevel(to, level - current)
                                    JewelManager.replaceOldJewel(to, jewel)
                                    player.sendMessage("${ChatColor.GREEN} >> ${ChatColor.WHITE}Set ${ChatColor.RED}${to.displayName}${ChatColor.WHITE}'s jewel level to ${ChatColor.RED}$level${ChatColor.WHITE}.")
                                }
                            }
                        }
                    }

                    literalArgument("add") {
                        entitySelectorArgumentOnePlayer("player") {
                            integerArgument("level") {
                                anyExecutor { player, args ->
                                    val to = args[0] as Player
                                    val level = args[1] as Int

                                    JewelManager.addToLevel(to, level)
                                    player.sendMessage("${ChatColor.GREEN} >> ${ChatColor.WHITE}Set ${ChatColor.RED}${to.displayName}${ChatColor.WHITE}'s jewel level to ${ChatColor.RED}$level${ChatColor.WHITE}.")
                                }
                            }
                        }
                    }

                    literalArgument("remove") {
                        entitySelectorArgumentOnePlayer("player") {
                            integerArgument("level") {
                                anyExecutor { player, args ->
                                    val to = args[0] as Player
                                    val level = args[1] as Int

                                    JewelManager.addToLevel(to, -level)
                                    player.sendMessage("${ChatColor.GREEN} >> ${ChatColor.WHITE}Set ${ChatColor.RED}${to.displayName}${ChatColor.WHITE}'s jewel level to ${ChatColor.RED}$level${ChatColor.WHITE}.")
                                }
                            }
                        }
                    }
                }

                literalArgument("jewel") {
                    literalArgument("set") {
                        entitySelectorArgumentOnePlayer("player") {
                            namespacedKeyArgument("jewel_type") {
                                replaceSuggestions(ArgumentSuggestions.strings(QuestRegistries.JEWEL.keySet().map { it.toString() }))

                                anyExecutor { sender, args ->
                                    val to = args[0] as Player
                                    val jewelType = args[1] as NamespacedKey

                                    val level = JewelManager.getOrCreateLevel(to)

                                    if (!giveJewelItem(to, sender, jewelType, level))
                                        throw CommandAPI.failWithString("Invalid jewel type!")
                                }
                            }
                        }
                    }
                }

                literalArgument("item") {
                    entitySelectorArgumentManyPlayers("player") {
                        namespacedKeyArgument("item") {
                            replaceSuggestions(ArgumentSuggestions.strings(QuestRegistries.CUSTOM_ITEM.keySet().map { it.toString() }))

                            integerArgument("count") {
                                anyExecutor { sender, args ->
                                    val to = args[0] as List<Player>
                                    val id = args[1] as NamespacedKey
                                    val count = args[2] as Int

                                    giveCustomItem(to, sender, id, count)
                                }
                            }

                            anyExecutor { sender, args ->
                                val to = args[0] as List<Player>
                                val id = args[1] as NamespacedKey

                                giveCustomItem(to, sender, id)
                            }
                        }
                    }
                }
            }
        }

        this.server.scheduler.runTaskTimer(this, Runnable {
            this.server.onlinePlayers.forEach {
                val jewel = JewelManager.getOrCreateJewel(it)
                jewel.displayCooldowns(it)
            }
        }, 0L, 20L)
    }

    private fun giveCustomItem(to: List<Player>, sender: CommandSender, key: NamespacedKey, count: Int = 1) {
        val id = ResourceLocation(key.namespace, key.key)
        val item = QuestRegistries.CUSTOM_ITEM.get(id)

        if (item == null) {
            sender.sendMessage("${ChatColor.RED}> Custom item $id does not exist!")
            return
        }

        val stack = item.getItem(count)

        to.forEach {
            it.world.dropItem(it.location, stack.asBukkitCopy())
        }
    }

    private fun giveJewelItem(to: Player, player: CommandSender, key: NamespacedKey, level: Int, count: Int = 1): Boolean {
        val jewelType = QuestRegistries.JEWEL.get(ResourceLocation(key.namespace, key.key)) ?: return false

        val oldJewel = JewelManager.getOrCreateJewel(to)
        JewelManager.resetAbilityEffects(to, oldJewel)
        JewelManager.setJewel(to, jewelType)

        player.sendMessage(
            Component.text("Successfully gave ")
                .append(to.name())
                .append(Component.text(" ${count}x "))
                .append(Component.translatable("jewels.${key.namespace}.${key.key}"))
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