package xyz.bluspring.onthequest.data

import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackCompatibility
import net.minecraft.server.packs.repository.PackRepository
import net.minecraft.server.packs.repository.PackSource
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import xyz.bluspring.onthequest.OnTheQuest
import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import java.lang.reflect.Field
import java.util.concurrent.CompletableFuture

object QuestDatapackManager {
    private const val PACK_NAME = "onthequest_data"
    private val availableField: Field

    init {
        val reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar()

        availableField = PackRepository::class.java.getDeclaredField(reflectionRemapper.remapFieldName(PackRepository::class.java, "available"))
        availableField.isAccessible = true
    }

    val builtinPack = Pack(PACK_NAME, true, {
        PluginPackResources()
    }, Component.literal("QuestSMP Built-in Datapack"), Component.literal("Built-in datapack resources for QuestSMP"),
        PackCompatibility.COMPATIBLE, Pack.Position.TOP, false, PackSource.BUILT_IN
    )

    fun load(): CompletableFuture<Void> {
        val server = (Bukkit.getServer() as CraftServer).handle.server
        val packRepository = server.packRepository

        // y'know, it would be fantastic if i had access to mixins.
        // but alas, i do not have that luxury with bukkit.
        // so i need to do this fuckin' stupid reflection hack instead.
        val availableMap = availableField.get(packRepository) as Map<String, Pack>

        val newAvailable = HashMap<String, Pack>(availableMap)
        newAvailable[builtinPack.id] = builtinPack

        // hoping and praying this works fine.
        availableField.set(packRepository, newAvailable)

        packRepository.setSelected(packRepository.selectedIds.toMutableList().apply {
            add(PACK_NAME)
        })

        return server.reloadResources(server.packRepository.selectedIds, ServerResourcesReloadedEvent.Cause.PLUGIN)
    }

    fun reload() {
        val server = (Bukkit.getServer() as CraftServer).handle.server

        // if the pack name got reset,
        // force load the datapack back in.
        if (!server.packRepository.isAvailable(PACK_NAME)) {
            load().thenAcceptAsync {
                if (OnTheQuest.debug) {
                    check()
                }
            }
        }
    }

    private fun check() {
        val server = (Bukkit.getServer() as CraftServer).handle.server

        server.resourceManager.listPacks().forEach {
            OnTheQuest.plugin.slF4JLogger.info("Loaded pack ${it.name}")
        }
    }
}