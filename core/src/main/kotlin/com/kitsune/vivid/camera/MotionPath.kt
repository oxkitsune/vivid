package com.kitsune.vivid.camera

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture

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

    fun linearPan(target: Location, ticks: Int): MotionPath {
        val future = CompletableFuture<Void>()

        path.add { camera ->

            // set state
            camera.state = Camera.State.BUSY

            object : BukkitRunnable() {

                val motionVector =
                    target.toVector().subtract(camera.location.toVector()).divide(Vector(ticks, ticks, ticks))
                val yawMotion = (target.yaw - camera.location.yaw) / ticks
                val pitchMotion = (target.pitch - camera.location.pitch) / ticks

                var timer = 0

                override fun run() {

                    // stop if destroyed
                    if (camera.state == Camera.State.DESTROYED) {
                        future.completeExceptionally(InterruptedException("Camera got destroyed!"))
                        cancel()
                        return
                    }

                    // compute new location
                    camera.location.add(motionVector)
                    camera.location.yaw += yawMotion
                    camera.location.pitch += pitchMotion

                    // update location
                    camera.entity.teleport(camera.location)

                    if (++timer >= ticks) {

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

    fun start(camera: Camera): CompletableFuture<Void> {

        // run each future, in sequence
        var current: CompletableFuture<Void> = CompletableFuture.completedFuture(null)
        path.forEach { motion ->
            current = current.thenCompose { motion.play(camera) }
        }

        return current
    }
}