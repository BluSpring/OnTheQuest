package xyz.bluspring.onthequest.data

import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.FolderPackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.VanillaPackResources
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.function.Predicate
import kotlin.io.path.toPath

class PluginPackResources : VanillaPackResources(
    PackMetadataSection(Component.literal("QuestSMP Built-in Resources"), 10),
    "questsmp"
) {
    override fun getResourceAsStream(type: PackType, id: ResourceLocation): InputStream? {
        val path = "/${type.directory}/${id.namespace}/${id.path}"

        return try {
            val url = PluginPackResources::class.java.getResource(path)
            if (isResourceUrlValid(url, path))
                url.openStream()
            else null
        } catch (_: IOException) {
            PluginPackResources::class.java.getResourceAsStream(path)
        }
    }

    override fun hasResource(type: PackType, id: ResourceLocation): Boolean {
        val path = "/${type.directory}/${id.namespace}/${id.path}"

        return try {
            val url = PluginPackResources::class.java.getResource(path)
            isResourceUrlValid(url, path)
        } catch (_: Exception) {
            false
        }
    }

    override fun getResources(
        type: PackType,
        namespace: String,
        prefix: String,
        allowedPathPredicate: Predicate<ResourceLocation>
    ): MutableCollection<ResourceLocation> {
        val set = mutableSetOf<ResourceLocation>()
        val root = PluginPackResources::class.java.getResource("/${type.directory}/.otqassetsroot").toURI().toPath().parent
        val path = root.resolve(namespace)

        val stream = Files.walk(path.resolve(prefix))

        stream.filter { !it.endsWith(".mcmeta") && Files.isRegularFile(it) }.mapMulti { it, consumer ->
            val normalized = path.relativize(it).toString().replace("\\\\", "/")
            val resourceLocation = ResourceLocation.tryBuild(namespace, normalized)
            if (resourceLocation == null) {
                Util.logAndPauseIfInIde("Invalid path in datapack: $namespace:$normalized, ignoring")
            } else {
                consumer.accept(resourceLocation)
            }
        }.filter(allowedPathPredicate).forEach {
            set.add(it)
        }

        return set
    }

    private fun isResourceUrlValid(url: URL?, path: String): Boolean {
        return url != null && (url.protocol == "jar" || FolderPackResources.validatePath(File(url.file), path))
    }

    override fun getResourceAsStream(path: String): InputStream? {
        return PluginPackResources::class.java.getResourceAsStream("/$path")
    }
}