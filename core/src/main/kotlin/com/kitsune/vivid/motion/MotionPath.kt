package com.kitsune.vivid.motion

import com.kitsune.vivid.camera.Camera
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.CompletableFuture
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MotionPath private constructor() {

    private val path: MutableList<(Camera) -> CompletableFuture<Camera>> = ArrayList()

    companion object {

        /**
         * Begin a new [MotionPath]
         *
         * @return the new, empty [MotionPath]
         */
        @JvmStatic
        fun begin(): MotionPath {
            return MotionPath()
        }

    }

    fun interpolate(target: Location, ticks: Int, type: Interpolation): MotionPath {
        val future = CompletableFuture<Camera>()

        path.add { camera ->

            // set state
            camera.state = Camera.State.BUSY

            object : BukkitRunnable() {

                var tickCount = 0
                val start = camera.location.clone()

                override fun run() {

                    // stop if destroyed
                    if (camera.isDestroyed()) {
                        future.completeExceptionally(InterruptedException("Camera got destroyed!"))
                        cancel()
                        return
                    }

                    val amount = tickCount++ / ticks.toDouble()

                    // compute new location
                    camera.location.x = type.apply(start.x, target.x, amount)
                    camera.location.y = type.apply(start.y, target.y, amount)
                    camera.location.z = type.apply(start.z, target.z, amount)
                    camera.location.yaw = type.apply(start.yaw, target.yaw, amount)
                    camera.location.pitch = type.apply(start.pitch, target.pitch, amount)

                    camera.entity.teleport(camera.location)

                    if (amount >= 1.0) {

                        // reset state
                        camera.state = Camera.State.WAITING
                        cancel()

                        future.complete(camera)
                    }
                }

            }.runTaskTimer(camera.plugin, 0, 1)

            future
        }

        return this
    }

    fun linearPan(target: Location, ticks: Int): MotionPath {
        return interpolate(target, ticks, Interpolation.LINEAR)
    }

    fun panAround(target: Location, radius: Double, height: Double = 0.0, ticks: Int, delay: Long = 0): MotionPath {
        val future = CompletableFuture<Camera>()

        path.add { camera ->
            camera.state = Camera.State.BUSY

            object : BukkitRunnable() {

                val stepSize = (2 * PI) / ticks
                var theta = 0.0

                override fun run() {

                    // stop if destroyed
                    if (camera.isDestroyed()) {
                        future.completeExceptionally(InterruptedException("Camera got destroyed!"))
                        return
                    }

                    val x = cos(theta) * radius
                    val z = sin(theta) * radius

                    camera.location.x = target.x + x
                    camera.location.y = target.y + height
                    camera.location.z = target.z + z

                    // compute direction to find pitch/yaw
                    val lookVec = target.toVector().subtract(camera.location.toVector()).normalize()
                    camera.location.pitch = Math.toDegrees(asin(-lookVec.y)).toFloat()

                    // I spent more time on this than I'd like to admit. fuck trigonometry
                    camera.location.yaw = Math.toDegrees(atan2(lookVec.z, lookVec.x)).toFloat() + 270f

                    camera.entity.teleport(camera.location)

                    theta += stepSize
                    if (theta >= 2 * PI) {

                        // reset state
                        camera.state = Camera.State.WAITING

                        future.complete(camera)
                        cancel()
                    }
                }

            }.runTaskTimer(camera.plugin, delay, 1)

            future
        }


        return this
    }

    fun wait(ticks: Long): MotionPath {
        val future = CompletableFuture<Camera>()

        path.add { camera ->

            camera.state = Camera.State.BUSY

            Bukkit.getScheduler().runTaskLater(camera.plugin, Runnable {

                // stop if destroyed
                if (camera.isDestroyed()) {
                    future.completeExceptionally(InterruptedException("Camera got destroyed!"))
                    return@Runnable
                }

                camera.state = Camera.State.WAITING
                future.complete(camera)

            }, ticks)
            future
        }

        return this
    }

    fun forEachViewer(function: (Player) -> Unit): MotionPath {
        path.add { camera ->
            camera.viewers.forEach { function.invoke(it) }
            CompletableFuture.completedFuture(camera)
        }

        return this
    }

    fun switchPosition(location: Location): MotionPath {

        path.add { camera ->
            camera.switchPosition(location)
            CompletableFuture.completedFuture(camera)
        }

        return this
    }

    /**
     * Run the specified lambda function at the current time, while executing the path
     *
     * @param function the function to invoke
     *
     * @return this motion path
     */
    fun run(function: () -> Unit): MotionPath {

        // run the specified function
        path.add {
            function.invoke()
            CompletableFuture.completedFuture(it)
        }

        return this
    }

    /**
     * Start the motion path with the specified [Camera]
     *
     * @param camera the camera to start the motion path for!
     *
     * @return a [CompletableFuture] that will complete once the path has completed!
     */
    fun start(camera: Camera): CompletableFuture<Camera> {

        // make sure to check the state
        if (camera.state != Camera.State.WAITING) {
            return CompletableFuture.failedFuture(IllegalStateException("That camera is not ready to play an animation!"))
        }

        // run each future, in sequence
        var current: CompletableFuture<Camera> = CompletableFuture.completedFuture(camera)

        path.forEach { motion ->
            current = current.thenCompose {
                motion.invoke(it).exceptionally { exception ->
                    current.completeExceptionally(exception)
                    return@exceptionally null
                }
            }
        }

        return current
    }
}