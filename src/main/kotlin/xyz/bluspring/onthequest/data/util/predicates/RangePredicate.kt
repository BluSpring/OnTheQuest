package xyz.bluspring.onthequest.data.util.predicates

import com.google.gson.JsonObject

abstract class RangePredicate<T : Comparable<T>>(
    val min: T,
    val max: T
) {
    class IntPredicate(min: Int, max: Int) : RangePredicate<Int>(min, max) {
        companion object {
            fun deserialize(json: JsonObject): IntPredicate {
                return IntPredicate(
                    if (json.has("min")) json.get("min").asInt else Int.MIN_VALUE,
                    if (json.has("max")) json.get("max").asInt else Int.MAX_VALUE
                )
            }
        }
    }

    class DoublePredicate(min: Double, max: Double) : RangePredicate<Double>(min, max){
        companion object {
            fun deserialize(json: JsonObject): DoublePredicate {
                return DoublePredicate(
                    if (json.has("min")) json.get("min").asDouble else Double.MIN_VALUE,
                    if (json.has("max")) json.get("max").asDouble else Double.MAX_VALUE
                )
            }
        }
    }

    class FloatPredicate(min: Float, max: Float) : RangePredicate<Float>(min, max){
        companion object {
            fun deserialize(json: JsonObject): FloatPredicate {
                return FloatPredicate(
                    if (json.has("min")) json.get("min").asFloat else Float.MIN_VALUE,
                    if (json.has("max")) json.get("max").asFloat else Float.MAX_VALUE
                )
            }
        }
    }

    fun isInRange(value: T): Boolean {
        return value in min..max
    }
}
