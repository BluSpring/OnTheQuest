package xyz.bluspring.onthequest.data.condition.types

import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.commands.arguments.NbtPathArgument
import net.minecraft.nbt.NumericTag
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import xyz.bluspring.onthequest.data.condition.Condition
import java.util.function.BiFunction

class CompareCondition(
    val path: NbtPathArgument.NbtPath,
    val comparison: Comparison,
    val value: Double
) : Condition() {
    override fun meetsCondition(player: Player): Boolean {
        val data = NbtPredicate.getEntityTagToCompare((player as CraftPlayer).handle)
        val paths = path.get(data)

        if (paths.isEmpty())
            return false

        if (paths.size > 1)
            throw IllegalStateException("Too many tags to compare! ($path)")

        val tag = paths[0]
        return if (tag is NumericTag) {
            comparison.test.apply(tag.asDouble, value)
        } else {
            throw IllegalStateException("Provided tag is not a valid numeric tag! ($path)")
        }
    }

    class Type : Condition.Type() {
        override fun parse(data: JsonObject): Condition {
            val nbtInput = data.get("input").asString
            val nbtPath = NbtPathArgument.nbtPath().parse(StringReader(nbtInput))

            val comparison = Comparison.values().first { data.get("comparison").asString == it.symbol }
            val value = data.get("value").asDouble

            return CompareCondition(nbtPath, comparison, value)
        }
    }

    enum class Comparison(val symbol: String, val test: BiFunction<Double, Double, Boolean>) {
        LOWER_THAN("<", { a, b -> a < b }),
        LOWER_THAN_OR_EQUAL("<=", { a, b -> a <= b }),
        EQUAL("==", { a, b -> a == b }),
        HIGHER_THAN_OR_EQUAL(">=", { a, b -> a >= b }),
        HIGHER_THAN(">", { a, b -> a > b })
    }
}