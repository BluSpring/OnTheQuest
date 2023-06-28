package xyz.bluspring.onthequest.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackCompatibility
import net.minecraft.server.packs.repository.PackRepository
import net.minecraft.server.packs.repository.PackSource
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.jewel.Jewel
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

    fun loadAllResources() {
        val server = (Bukkit.getServer() as CraftServer).handle.server
        server.resourceManager.listResources("abilities") {
            it.path.endsWith(".json")
        }.forEach { (location, resource) ->
            try {
                val json = JsonParser.parseReader(resource.openAsReader()).asJsonObject

                val abilityType = QuestRegistries.ABILITY_TYPE.get(ResourceLocation.tryParse(json.get("type").asString))!!

                Registry.register(QuestRegistries.ABILITY, location, abilityType.create(
                    if (json.has("data"))
                        json.getAsJsonObject("data")
                    else
                        JsonObject(),
                    if (json.has("cooldown"))
                        json.get("cooldown").asLong
                    else
                        0L
                ))
            } catch (e: Exception) {
                OnTheQuest.logger.error("Failed to load ability resource $location!")
                e.printStackTrace()
            }
        }

        server.resourceManager.listResources("jewels") {
            it.path.endsWith(".json")
        }.forEach { (location, resource) ->
            try {
                val json = JsonParser.parseReader(resource.openAsReader()).asJsonObject
                val id = ResourceLocation(location.namespace, location.path.split("/").last())

                Registry.register(QuestRegistries.JEWEL, id, Jewel.deserialize(json, id))
            } catch (e: Exception) {
                OnTheQuest.logger.error("Failed to load jewel resource $location!")
                e.printStackTrace()
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