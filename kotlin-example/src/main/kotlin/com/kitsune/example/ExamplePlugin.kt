package com.kitsune.example

import com.kitsune.vivid.camera.Camera
import com.kitsune.vivid.camera.MotionPath
import com.kitsune.vivid.camera.Interpolation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.TitlePart
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class ExamplePlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {

        val camera = Camera(event.player.eyeLocation, this)
        val start = event.player.eyeLocation.clone()

        // add the player as viewer of the camera
        camera.addViewer(event.player)

        MotionPath.begin()
            .linearPan(start.clone().add(10.0, 10.0, 0.0), 80) // linearly pan to new location
            .wait(80) // wait 80 ticks
            .interpolate(start.clone().add(100.0, 10.0, 0.0), 100, Interpolation.SQUARED) // use a different interpolation function
            .switchPosition(start.clone().add(5.0, 0.0, 5.0)) // switch the position of the camera
            .forEachViewer { viewer ->

                // send each viewer of the camera the following title
                viewer.sendTitlePart(TitlePart.TITLE, Component.text("Welcome to the server!", NamedTextColor.GRAY))
            }
            .start(camera) // move the camera along the created path
            .thenAccept { camera.destroy() } // when the animation is complete, destroy the camera
            .exceptionally { exception ->

                // something went wrong!
                logger.warning("Failed to complete camera path!: ${exception.message}")

                null
            }

    }
}