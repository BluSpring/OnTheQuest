package xyz.bluspring.onthequest.data

import com.google.gson.JsonParser
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.ability.Ability
import xyz.bluspring.onthequest.data.ability.AbilityTypes
import xyz.bluspring.onthequest.data.jewel.Jewel
import xyz.bluspring.onthequest.data.quests.QuestManager
import xyz.bluspring.onthequest.data.util.KeybindType
import java.io.File

object QuestDatapackManager {
    const val CURRENT_PACK_VERSION = 12
    private var triggerReload = false

    fun reload() {
        val server = (OnTheQuest.plugin.server as CraftServer).handle.server
        val mainWorldDir = server.storageSource.levelDirectory.dataFile().toFile().parentFile

        val datapackDir = File(mainWorldDir, "datapacks")
        if (!datapackDir.exists())
            datapackDir.mkdirs()

        val versionFile = File(datapackDir, "OTQ_CURRENT_VERSION")

        val currentVersion = if (versionFile.exists())
            versionFile.readText().toInt()
        else
            0

        if (currentVersion == CURRENT_PACK_VERSION)
            return

        val rootFile = OnTheQuest::class.java.getResource("/resources.zip")
        val datapack = File(datapackDir, "otq_DONOTMODIFY.zip")

        if (!datapack.exists())
            datapack.createNewFile()

        datapack.writeBytes(rootFile!!.readBytes())
        versionFile.writeText("$CURRENT_PACK_VERSION")
        triggerReload = true
    }

    fun loadAllResources() {
        QuestRegistries.ABILITY_TYPE.forEach {
            it.clear()
        }

        AbilityTypes.init()

        val server = (Bukkit.getServer() as CraftServer).handle.server

        server.resourceManager.listPacks().forEach { pack ->
            loadFromPack(pack)
        }

        if (triggerReload) {
            OnTheQuest.logger.info("Automatically installed an update to the internal OnTheQuest datapack, triggering a restart.")
            server.stopServer()
        }
    }

    private fun loadFromPack(pack: PackResources) {
        try {
            val q = ResourceLocation("questsmp", "quests.json")
            if (pack.hasResource(PackType.SERVER_DATA, q))
                pack.getResource(PackType.SERVER_DATA, q).run {
                    try {
                        val json = JsonParser.parseReader(this.reader()).asJsonObject

                        QuestManager.parseFromJson(json)
                        OnTheQuest.logger.info("Loaded quests.json")
                    } catch (e: Exception) {
                        OnTheQuest.logger.error("Failed to load quests.json!")
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            pack.getResources(PackType.SERVER_DATA, "questsmp", "abilities") { it.path.endsWith(".json") }
                .forEach { location ->
                    try {
                        val resource = pack.getResource(PackType.SERVER_DATA, location)

                        val json = JsonParser.parseReader(resource.reader()).asJsonObject
                        val id = ResourceLocation(location.namespace, location.path.removePrefix("abilities/").removeSuffix(".json"))

                        val abilityType =
                            QuestRegistries.ABILITY_TYPE.get(ResourceLocation.tryParse(json.get("type").asString))!!

                        val ability = Registry.register(QuestRegistries.ABILITY, id, Ability.parse(json)
                            .apply {
                                if (json.has("keybind")) {
                                    val keybindData = json.getAsJsonObject("keybind")

                                this.keybindType = KeybindType.fromKey(keybindData.get("key").asString)
                            }
                        })

                        abilityType.abilities.add(ability)
                        OnTheQuest.logger.info("Registered ability $id")
                    } catch (e: Exception) {
                        OnTheQuest.logger.error("Failed to load ability resource $location!")
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            pack.getResources(PackType.SERVER_DATA, "questsmp", "jewels") { it.path.endsWith(".json") }
                .forEach { location ->
                    val resource = pack.getResource(PackType.SERVER_DATA, location)
                    try {
                        val json = JsonParser.parseReader(resource.reader()).asJsonObject
                        val id = ResourceLocation(location.namespace, location.path.removePrefix("jewels/").removeSuffix(".json"))

                        Registry.register(QuestRegistries.JEWEL, id, Jewel.deserialize(json, id))
                        OnTheQuest.logger.info("Registered jewel $id")
                    } catch (e: Exception) {
                        OnTheQuest.logger.error("Failed to load jewel resource $location!")
                        e.printStackTrace()
                    }
                }
        } catch (_: Exception) {}
    }
}