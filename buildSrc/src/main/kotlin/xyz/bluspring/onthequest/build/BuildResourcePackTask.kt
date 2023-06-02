package xyz.bluspring.onthequest.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

abstract class BuildResourcePackTask : DefaultTask() {
    init {
        this.group = "setup"
    }

    @TaskAction
    fun buildResources() {
        val packVersion = this.project.property("resource_pack_version")

        val resources = File(this.project.rootDir, "common/src/main/resources/resourcepack")
        val output = File(this.project.buildDir, "resource_packs/OnTheQuest Resources v$packVersion.zip")

        if (!output.parentFile.exists())
            output.parentFile.mkdirs()

        val zipStream = ZipOutputStream(output.outputStream())

        resources.walkTopDown().forEach {
            if (it.isDirectory)
                return@forEach

            val path = it.canonicalPath.replace(resources.canonicalPath, "").removePrefix("\\").replace("\\", "/")

            val entry = ZipEntry(path)
            zipStream.putNextEntry(entry)
            zipStream.write(it.readBytes())
            zipStream.closeEntry()
        }

        zipStream.close()

        println("Successfully created OnTheQuest Resources v$packVersion.zip")
    }
}