package com.kitsune.example;

import com.kitsune.vivid.camera.Camera;
import com.kitsune.vivid.camera.properties.CameraProperties;
import com.kitsune.vivid.camera.properties.DefaultProperties;
import com.kitsune.vivid.motion.MotionPath;
import kotlin.Unit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        // register listeners
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {

        // only trigger when player is sneaking
        if (event.isSneaking()) return;

        var player = event.getPlayer();
        var camera = new Camera.Builder(this)
                .location(player.getEyeLocation())
                .disconnectHandler(DefaultProperties.DisconnectHandlers.REJOIN)
                .build();
        var start = player.getEyeLocation().clone();

        // set the pitch and yaw
        start.setPitch(90);
        start.setYaw(90);

        // add player to camera
        camera.addViewer(player);

        // create camera motion path
        MotionPath
                .begin()
                .linearPan(start.clone().add(0d, 10d, 0d), 30)
                .forEachViewer(viewer -> {

                    // send this message to each viewer
                    viewer.sendMessage(Component.text("Hello viewer!", NamedTextColor.GRAY));

                    // return Unit.INSTANCE, as we don't have Unit in Java
                    return Unit.INSTANCE;
                })
                .start(camera)// start the motion
                .thenAccept(v -> {
                    camera.destroy(GameMode.CREATIVE);
                })
                .exceptionally(ex -> {

                    // destroy camera if smth goes wrong
                    camera.destroy(GameMode.CREATIVE);
                    return null;
                });

    }
}
