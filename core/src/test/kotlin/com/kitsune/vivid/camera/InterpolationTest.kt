package com.kitsune.vivid.camera

import org.bukkit.util.Vector
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InterpolationTest {


    @Test
    fun rangeCheckTest() {
        Assertions.assertThrowsExactly(
            IllegalStateException::class.java,
            { Interpolation.LINEAR.apply(0.0, 1.0, 1.5) },
            "Failed range check!"
        )

        Assertions.assertThrowsExactly(
            IllegalStateException::class.java,
            { Interpolation.LINEAR.apply(0.0, 1.0, -10.0) },
            "Failed range check!"
        )
    }

    @Test
    fun linearInterpolationTest() {
        Assertions.assertEquals(
            0.5,
            Interpolation.LINEAR.apply(0.0, 1.0, 0.5),
            "Linear interpolation calculated wrongly!"
        )
        Assertions.assertEquals(
            0.75,
            Interpolation.LINEAR.apply(0.0, 1.0, 0.75),
            "Linear interpolation calculated wrongly!"
        )
        Assertions.assertEquals(
            0.333333333,
            Interpolation.LINEAR.apply(0.0, 1.0, 0.333333333),
            "Linear interpolation calculated wrongly!"
        )
    }

    @Test
    fun vectorLinearInterpolationTest() {
        val fromVec = Vector(0.0, 0.0, 0.0)
        val toVec = Vector(100.0, 100.0, 100.0)

        Assertions.assertEquals(
            Vector(50.0, 50.0, 50.0),
            Interpolation.LINEAR.apply(fromVec, toVec, 0.5),
            "Linear Interpolation failed for vector!"
        )
        Assertions.assertEquals(
            Vector(1.0, 1.0, 1.0),
            Interpolation.LINEAR.apply(fromVec, toVec, 0.01),
            "Linear Interpolation failed for vector!"
        )
    }

}