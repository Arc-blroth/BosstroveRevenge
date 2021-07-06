package ai.arcblroth.boss.level

import ai.arcblroth.boss.Engine
import ai.arcblroth.boss.backend.EventLoop
import ai.arcblroth.boss.load.LoadProcess
import ai.arcblroth.boss.render.Scene
import org.joml.Matrix4f

class LevelEngine(private val load: LoadProcess) : Engine {
    init {
        load.testingRoom.transform = Matrix4f().translation(-100.0f, -50.0f, 50.0f)
    }

    override fun step(eventLoop: EventLoop, lastFrameTime: Long): Pair<Scene, Engine> {
        val scene = Scene(sceneMeshes = arrayListOf(load.testingRoom))
        return Pair(scene, this)
    }
}
