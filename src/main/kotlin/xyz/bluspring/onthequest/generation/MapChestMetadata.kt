package xyz.bluspring.onthequest.generation

import org.bukkit.metadata.MetadataValue
import org.bukkit.plugin.Plugin
import xyz.bluspring.onthequest.OnTheQuest
import java.util.UUID

data class MapChestMetadata(
    val uuid: UUID
) : MetadataValue {
    override fun value(): Any {
        return uuid
    }

    override fun asInt(): Int {
        TODO("Not yet implemented")
    }

    override fun asFloat(): Float {
        TODO("Not yet implemented")
    }

    override fun asDouble(): Double {
        TODO("Not yet implemented")
    }

    override fun asLong(): Long {
        TODO("Not yet implemented")
    }

    override fun asShort(): Short {
        TODO("Not yet implemented")
    }

    override fun asByte(): Byte {
        TODO("Not yet implemented")
    }

    override fun asBoolean(): Boolean {
        TODO("Not yet implemented")
    }

    override fun asString(): String {
        return uuid.toString()
    }

    override fun getOwningPlugin(): Plugin {
        return OnTheQuest.plugin
    }

    override fun invalidate() {
    }
}
