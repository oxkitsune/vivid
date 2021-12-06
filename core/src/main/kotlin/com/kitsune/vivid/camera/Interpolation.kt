package com.kitsune.vivid.camera

import org.bukkit.util.Vector

enum class Interpolation(private val function: (Double, Double, Double) -> Double) {

    LINEAR({ from, to, amount -> from + ((to - from) * amount) });

    fun apply (from: Double, to: Double, amount: Double): Double {
        check(amount in 0.0..1.0) { "Interpolation amount must be in range 0.0..1.0!" }
        return function.invoke(from, to, amount)
    }

    fun apply (from: Vector, to: Vector, amount: Double): Vector {
        check(amount in 0.0..1.0) { "Interpolation amount must be in range 0.0..1.0!" }
        return Vector(
            function.invoke(from.x, to.x, amount),
            function.invoke(from.y, to.y, amount),
            function.invoke(from.z, to.z, amount),
        )
    }
}
