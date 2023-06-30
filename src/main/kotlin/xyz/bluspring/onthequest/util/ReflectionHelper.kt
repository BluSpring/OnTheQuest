package xyz.bluspring.onthequest.util

import xyz.jpenilla.reflectionremapper.ReflectionRemapper

object ReflectionHelper {
    val reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar()
}