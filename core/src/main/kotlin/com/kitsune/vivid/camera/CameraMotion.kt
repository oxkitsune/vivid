package com.kitsune.vivid.camera

import java.util.concurrent.CompletableFuture

fun interface CameraMotion {
    fun play (camera: Camera): CompletableFuture<Camera>
}