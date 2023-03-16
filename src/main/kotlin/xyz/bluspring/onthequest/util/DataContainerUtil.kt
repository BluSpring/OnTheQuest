package xyz.bluspring.onthequest.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer

object DataContainerUtil {
    fun parseKeys(key: NamespacedKey, container: PersistentDataContainer): List<NamespacedKey> {
        if (!container.has(key))
            return emptyList()

        val keyList = container.get(key, StringArrayDataType()) ?: return emptyList()

        return keyList.map { NamespacedKey.fromString(it)!! }.toList()
    }
}