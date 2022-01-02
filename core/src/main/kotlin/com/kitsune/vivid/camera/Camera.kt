package com.kitsune.vivid.camera

import com.kitsune.vivid.camera.properties.CameraProperties
import com.kitsune.vivid.camera.properties.DisconnectHandler
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Bat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin

class Camera private constructor(
    val properties: CameraProperties,
    internal val location: Location,
    internal val plugin: Plugin
) {

    internal var entity: Bat = createCameraEntity(location)
    internal val viewers: MutableList<Player> = ArrayList()
    internal var state = State.WAITING
    private val listener = CameraListener(this, plugin)

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
     * Remove a viewer from this [Camera].
     * This will set the [Player]'s [GameMode] to the [GameMode] specified, and teleport
     * the player to the [Location] specified!
     *
     * @param player the viewer to remove
     * @param location the location to teleport the player to
     * @param gameMode the [GameMode] the viewer will be set to
     */
    fun removeViewer(player: Player, location: Location = entity.location, gameMode: GameMode = GameMode.SURVIVAL) {
        check(viewers.contains(player)) { "${player.name} is not a viewer of this camera!" }

        viewers.remove(player)

        // reset the player's spectator state
        player.spectatorTarget = null
        player.gameMode = gameMode
    }

    /**
     * Check if the specified [Player] is viewer of this [Camera]
     *
     * @param player the player to check
     *
     * @return `true` if the player is a viewer or else `false`
     */
    fun isViewer (player: Player): Boolean = viewers.contains(player)

    /**
     * Destroys the [Camera] and sets all the viewers to the specified [GameMode]
     *
     * @param gameMode the [GameMode] the viewers will be set to
     */
    fun destroy(gameMode: GameMode = GameMode.SURVIVAL) {

        state = State.DESTROYED

        viewers.forEach {

            if (it.gameMode == GameMode.SPECTATOR) {
                it.spectatorTarget = null
            }

            it.gameMode = gameMode
        }

        entity.remove()
        HandlerList.unregisterAll(listener)
    }

    /**
     * Get whether this [Camera] has been destroyed
     */
    fun isDestroyed() = state == State.DESTROYED

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

    data class Builder(
        private val plugin: Plugin,
        private var location: Location? = null,
        private val properties: CameraProperties = CameraProperties()
    ) {

        constructor(plugin: Plugin) : this(plugin, null, CameraProperties())

        fun location(location: Location) = apply { this.location = location }

        fun disconnectHandler(disconnectHandler: DisconnectHandler) =
            apply { this.properties.disconnectHandler = disconnectHandler }

        fun build() = Camera(properties, location!!, plugin)
    }

    /**
     * The state of the [Camera]
     */
    enum class State {
        WAITING,
        BUSY,
        DESTROYED
    }
}