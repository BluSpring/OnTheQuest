package xyz.bluspring.onthequest.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftServer
import xyz.bluspring.onthequest.OnTheQuest
import xyz.bluspring.onthequest.data.jewel.Jewel
import xyz.bluspring.onthequest.data.quests.QuestManager
import xyz.bluspring.onthequest.data.util.KeybindType
import java.io.File

object QuestDatapackManager {
    const val CURRENT_PACK_VERSION = 1

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

        val rootFile = OnTheQuest::class.java.getResource("/otq_DONOTMODIFY.zip")
        val datapack = File(datapackDir, "otq_DONOTMODIFY.zip")

        if (!datapack.exists())
            datapack.createNewFile()

        datapack.writeBytes(rootFile!!.readBytes())
        versionFile.writeText("$CURRENT_PACK_VERSION")
    }

    fun loadAllResources() {
        QuestRegistries.ABILITY_TYPE.forEach {
            it.clear()
        }

        val server = (Bukkit.getServer() as CraftServer).handle.server

        server.resourceManager.getResource(ResourceLocation("questsmp", "quests.json")).ifPresent {
            try {
                val json = JsonParser.parseReader(it.openAsReader()).asJsonObject

                QuestManager.parseFromJson(json)
                OnTheQuest.logger.info("Loaded quests.json")
            } catch (e: Exception) {
                OnTheQuest.logger.error("Failed to load quests.json!")
                e.printStackTrace()
            }
        }

        server.resourceManager.listResources("abilities") {
            it.path.endsWith(".json")
        }.forEach { (location, resource) ->
            try {
                val json = JsonParser.parseReader(resource.openAsReader()).asJsonObject

                val abilityType = QuestRegistries.ABILITY_TYPE.get(ResourceLocation.tryParse(json.get("type").asString))!!

                val ability = Registry.register(QuestRegistries.ABILITY, location, abilityType.create(
                    if (json.has("data"))
                        json.getAsJsonObject("data")
                    else
                        JsonObject(),
                    if (json.has("cooldown"))
                        json.get("cooldown").asLong
                    else
                        0L
                ).apply {
                    if (json.has("keybind")) {
                        val keybindData = json.getAsJsonObject("keybind")

                        this.keybindType = KeybindType.fromKey(keybindData.get("key").asString)
                    }
                })

                abilityType.abilities.add(ability)
                OnTheQuest.logger.info("Registered ability $location")
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
                OnTheQuest.logger.info("Registered jewel $id")
            } catch (e: Exception) {
                OnTheQuest.logger.error("Failed to load jewel resource $location!")
                e.printStackTrace()
            }
        }
    }
}