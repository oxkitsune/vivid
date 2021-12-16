package com.kitsune.vivid.camera

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

    private val path: MutableList<CameraMotion> = ArrayList()

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

    fun interpolate (target: Location, ticks: Int, type: Interpolation): MotionPath {
        val future = CompletableFuture<Void>()

        path.add { camera ->

            // set state
            camera.state = Camera.State.BUSY

            object : BukkitRunnable() {

                var tickCount = 0.0
                val start = camera.location

                override fun run() {

                    // stop if destroyed
                    if (camera.state == Camera.State.DESTROYED) {
                        future.completeExceptionally(InterruptedException("Camera got destroyed!"))
                        cancel()
                        return
                    }

                    val amount = tickCount++/ticks

                    // compute new location
                    camera.location.add(type.apply(start.toVector(), target.toVector(), amount))
                    camera.location.yaw += type.apply(start.yaw, target.yaw, amount)
                    camera.location.pitch += type.apply(start.pitch, target.pitch, amount)

                    camera.entity.teleport(camera.location)

                    if (amount >= 1.0) {

                        // reset state
                        camera.state = Camera.State.WAITING
                        cancel()

                        future.complete(null)
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

    fun panAround (target: Location, radius: Double, height: Double = 0.0, ticks: Int, delay: Long = 0): MotionPath {
        val future = CompletableFuture<Void>()

        path.add { camera ->
            camera.state = Camera.State.BUSY

            object : BukkitRunnable () {

                val stepSize = (2 * PI) / ticks
                var theta = 0.0

                override fun run() {

                    // stop if destroyed
                    if (camera.state == Camera.State.DESTROYED) {
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

                        future.complete(null)
                        cancel()

                    }
                }

            }.runTaskTimer(camera.plugin, delay, 1)

            future
        }


        return this
    }

    fun wait(ticks: Long): MotionPath {
        val future = CompletableFuture<Void>()

        path.add { camera ->

            camera.state = Camera.State.BUSY

            Bukkit.getScheduler().runTaskLater(camera.plugin, Runnable {

                // stop if destroyed
                if (camera.state == Camera.State.DESTROYED) {
                    future.completeExceptionally(InterruptedException("Camera got destroyed!"))
                    return@Runnable
                }

                camera.state = Camera.State.WAITING
                future.complete(null)

            }, ticks)
            future
        }

        return this
    }

    fun forEachViewer(function: (Player) -> Unit): MotionPath {
        path.add { camera ->
            camera.viewers.forEach { function.invoke(it) }
            CompletableFuture.completedFuture(null)
        }

        return this
    }

    fun switchPosition(location: Location): MotionPath {

        path.add { camera ->
            camera.switchPosition(location)
            CompletableFuture.completedFuture(null)
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
    fun run (function: () -> Unit): MotionPath {

        // run the specified function
        path.add {
            function.invoke()
            CompletableFuture.completedFuture(null)
        }

        return this
    }

    fun start(camera: Camera): CompletableFuture<Void> {

        // run each future, in sequence
        var current: CompletableFuture<Void> = CompletableFuture.completedFuture(null)
        path.forEach { motion ->
            current = current.thenCompose { motion.play(camera) }
        }

        return current
    }
}