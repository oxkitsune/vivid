package com.kitsune.vivid.motion

import org.bukkit.util.Vector

enum class Interpolation(private val function: (Double, Double, Double) -> Double) {

    /**
     * Linear interpolation, linearly interpolate between the two specified values.
     *
     * `from + (to - from) * amount`
     */
    LINEAR({ from, to, amount -> from + ((to - from) * amount) }),

    /**
     * Squared interpolation, interpolate between the two specified values using a square function.
     *
     * `from + (to - from) * amount^2`
     */
    SQUARED({ from, to, amount  -> from + ((to - from) * (amount * amount)) }),

    /**
     * Square root interpolation, interpolate between the two specified values using a square root function.
     *
     * `from + (to - from) * sqrt(amount)`
     */
    SQRT({ from, to, amount -> from + ((to - from) * (kotlin.math.sqrt(amount))) });


    /**
     * Apply this interpolation function to the specified values and return the outcome.
     *
     * @param from the value to start at, `x1`
     * @param to the end value of the interpolation, `x2`
     * @param amount the completion rate of the interpolation, a value between `0.0` and `1.0`
     *
     * @return the interpolated value between `x1` and `x2` for the specified `amount`
     */
    fun apply (from: Double, to: Double, amount: Double): Double {
        check(amount in 0.0..1.0) { "Interpolation amount must be in range 0.0..1.0!" }
        return function.invoke(from, to, amount)
    }

    /**
     * Apply this interpolation function to the specified values and return the outcome.
     *
     * @param from the value to start at, `x1`
     * @param to the end value of the interpolation, `x2`
     * @param amount the completion rate of the interpolation, a value between `0.0` and `1.0`
     *
     * @return the interpolated value between `x1` and `x2` for the specified `amount`
     */
    fun apply (from: Float, to: Float, amount: Double): Float {
        check(amount in 0.0..1.0) { "Interpolation amount must be in range 0.0..1.0!" }
        return function.invoke(from.toDouble(), to.toDouble(), amount).toFloat()
    }

    /**
     * Apply this interpolation function to the specified values and return the outcome.
     *
     * @param from the value to start at, `x1`
     * @param to the end value of the interpolation, `x2`
     * @param amount the completion rate of the interpolation, a value between `0.0` and `1.0`
     *
     * @return the interpolated value between `x1` and `x2` for the specified `amount`
     */
    fun apply (from: Vector, to: Vector, amount: Double): Vector {
        check(amount in 0.0..1.0) { "Interpolation amount must be in range 0.0..1.0!" }
        return Vector(
            function.invoke(from.x, to.x, amount),
            function.invoke(from.y, to.y, amount),
            function.invoke(from.z, to.z, amount),
        )
    }
}
