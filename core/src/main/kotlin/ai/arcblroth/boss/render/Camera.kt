package ai.arcblroth.boss.render

import org.joml.Vector3d

/**
 * A perspective projection camera, used to
 * render the [sceneMeshes][Scene.sceneMeshes]
 * in a Scene.
 */
data class Camera(
    var pos: Vector3d = Vector3d(),
    var yaw: Double = 0.0,
    var pitch: Double = 0.0,
    var fov: Double = 45.0,
)
