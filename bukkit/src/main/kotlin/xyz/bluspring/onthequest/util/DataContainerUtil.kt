package xyz.bluspring.onthequest.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object DataContainerUtil {
    fun parseKeys(key: NamespacedKey, container: PersistentDataContainer): List<NamespacedKey> {
        if (!container.has(key))
            return emptyList()

        if (container.has(key, PersistentDataType.STRING)) {
            val keyString = container.get(key, PersistentDataType.STRING)!!

            return listOf(NamespacedKey.fromString(keyString)!!)
        }

        val keyList = container.get(key, StringArrayDataType()) ?: return emptyList()

        return keyList.map { NamespacedKey.fromString(it)!! }.toList()
    }
}