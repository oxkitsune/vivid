package com.kitsune.vivid.camera.properties

import com.kitsune.vivid.camera.Camera
import org.bukkit.entity.Player
import java.util.Collections
import java.util.UUID

class DefaultProperties {

    object DisconnectHandlers {

        @JvmField
        val REJOIN = object : DisconnectHandler {

            val playerCache: MutableList<UUID> = Collections.synchronizedList(ArrayList())

            override fun onQuit(camera: Camera, player: Player) {

                // make sure camera isn't destroyed
                if (camera.isDestroyed()) return

                // remove viewer from camera
                camera.removeViewer(player)

                // store player uuid in cache
                playerCache.add(player.uniqueId)
            }

            override fun onJoin(camera: Camera, player: Player) {

                // make sure camera isn't destroyed
                if (camera.isDestroyed()) return

                // add the viewer again
                camera.addViewer(player)

                // remove player from the cache
                playerCache.remove(player.uniqueId)
            }
        }
    }

}