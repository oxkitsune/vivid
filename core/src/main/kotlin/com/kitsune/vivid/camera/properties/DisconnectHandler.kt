package com.kitsune.vivid.camera.properties

import com.kitsune.vivid.camera.Camera
import org.bukkit.entity.Player

interface DisconnectHandler {

    fun onQuit (camera: Camera, player: Player)

    fun onJoin (camera: Camera, player: Player)
}