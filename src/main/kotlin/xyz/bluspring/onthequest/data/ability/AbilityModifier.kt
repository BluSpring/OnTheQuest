package xyz.bluspring.onthequest.data.ability

import com.google.gson.JsonObject
import net.minecraft.util.RandomSource

abstract class AbilityModifier<T : Number>(protected val min: T, protected val max: T) {
    abstract fun get(original: T): T
    abstract fun getValue(): T

    abstract class IntModifier(min: Int, max: Int) : AbilityModifier<Int>(min, max) {
        override fun getValue(): Int {
            return random.nextInt(min, max)
        }

        class Add(min: Int, max: Int) : IntModifier(min, max) {
            constructor(base: Int) : this(base, base)

            override fun get(original: Int): Int {
                return original + getValue()
            }
        }

        class Multiply(min: Int, max: Int) : IntModifier(min, max) {
            constructor(base: Int) : this(base, base)

            override fun get(original: Int): Int {
                return original * getValue()
            }
        }

        companion object {
            fun parse(data: JsonObject): IntModifier {
                val type = data.get("type").asString

                val min = if (data.has("value"))
                    data.get("value").asInt
                else
                    data.get("min").asInt

                val max = if (data.has("value"))
                    data.get("value").asInt
                else
                    data.get("max").asInt

                return when (type.lowercase()) {
                    "add" -> Add(min, max)
                    "multiply" -> Multiply(min, max)
                    else -> throw IllegalStateException("Invalid modifier type $type!")
                }
            }
        }
    }

    abstract class DoubleModifier(min: Double, max: Double) : AbilityModifier<Double>(min, max) {
        override fun getValue(): Double {
            return random.nextDouble() * (max - min) + min
        }

        class Add(min: Double, max: Double) : DoubleModifier(min, max) {
            constructor(base: Double) : this(base, base)

            override fun get(original: Double): Double {
                return original + getValue()
            }
        }

        class Multiply(min: Double, max: Double) : DoubleModifier(min, max) {
            constructor(base: Double) : this(base, base)

            override fun get(original: Double): Double {
                return original * getValue()
            }
        }

        companion object {
            fun parse(data: JsonObject): DoubleModifier {
                val type = data.get("type").asString

                val min = if (data.has("value"))
                    data.get("value").asDouble
                else
                    data.get("min").asDouble

                val max = if (data.has("value"))
                    data.get("value").asDouble
                else
                    data.get("max").asDouble

                return when (type.lowercase()) {
                    "add" -> Add(min, max)
                    "multiply" -> Multiply(min, max)
                    else -> throw IllegalStateException("Invalid modifier type $type!")
                }
            }
        }
    }

    companion object {
        private val random = RandomSource.create()
    }
}