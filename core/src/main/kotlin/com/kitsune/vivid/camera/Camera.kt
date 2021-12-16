package com.kitsune.vivid.camera

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Bat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class Camera(internal val location: Location, internal val plugin: Plugin) : Listener {

    internal var entity: Bat = createCameraEntity(location)
    internal val viewers: MutableList<Player> = ArrayList()
    internal var state = State.WAITING

    init {

        // register listener
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    /**
     * Add a viewer to this [Camera].
     * This will set the [Player]'s [GameMode] to [GameMode.SPECTATOR]
     *
     * @param player the player to add as viewer
     */
    fun addViewer(player: Player) {
        if (viewers.contains(player)) return
        viewers.add(player)

        player.gameMode = GameMode.SPECTATOR
        player.spectatorTarget = entity
    }

    /**
     * Destroys the [Camera] and sets all the viewers to the specified [GameMode]
     *
     * @param gameMode the [GameMode] the viewers will be set to
     */
    fun destroy(gameMode: GameMode = GameMode.SURVIVAL) {

        state = State.DESTROYED

        viewers.forEach {
            it.spectatorTarget = null
            it.gameMode = gameMode
        }

        entity.remove()
        HandlerList.unregisterAll(this)
    }

    /**
     * Switch the position of the [Camera]
     *
     * @param newLocation the new location of the [Camera]
     */
    fun switchPosition(newLocation: Location): Camera {
        val newCamera = createCameraEntity(newLocation)

        // update location
        location.set(newLocation.x, newLocation.y, newLocation.z)
        location.yaw = newLocation.yaw
        location.pitch = newLocation.pitch

        // switch all viewers over
        viewers.forEach { it.spectatorTarget = newCamera }

        // remove old entity
        entity.remove()
        entity = newCamera
        return this
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onSpectatorTargetSwitchEvent(event: PlayerStopSpectatingEntityEvent) {
        if (state == State.DESTROYED) return
        if (event.spectatorTarget.uniqueId != entity.uniqueId) return
        if (!viewers.contains(event.player)) return

        event.isCancelled = true
    }

    /**
     * Create a new [Camera] entity at the specified [Location]
     *
     * @param location the location to create the [Camera] entity at
     *
     * @return the [Bat] entity that got created
     */
    private fun createCameraEntity(location: Location): Bat {
        val entity = location.world.spawnEntity(location, EntityType.BAT) as Bat

        entity.isInvisible = true
        entity.isSilent = true
        entity.isInvulnerable = true
        entity.isAwake = true
        entity.setAI(false)
        entity.setGravity(false)

        return entity
    }

    enum class State {
        WAITING,
        BUSY,
        DESTROYED
    }
}