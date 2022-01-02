package com.kitsune.vivid.camera

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class CameraListener(private val camera: Camera, private val plugin: Plugin) : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onSpectatorTargetSwitchEvent(event: PlayerStopSpectatingEntityEvent) {
        if (camera.isDestroyed()) return
        if (event.spectatorTarget.uniqueId != camera.entity.uniqueId) return
        if (!camera.isViewer(event.player)) return

        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onPlayerQuitEvent (event: PlayerQuitEvent) {
        if (camera.isDestroyed()) return
        if (!camera.isViewer(event.player)) return

        camera.properties.disconnectHandler?.onQuit(camera, event.player)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onPlayerJoinEvent (event: PlayerJoinEvent) {
        if (camera.isDestroyed()) return

        camera.properties.disconnectHandler?.onJoin(camera, event.player)
    }
}