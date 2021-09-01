package ai.arcblroth.boss.level

import ai.arcblroth.boss.Engine
import ai.arcblroth.boss.anim.AnimationController
import ai.arcblroth.boss.anim.OffsetIndex
import ai.arcblroth.boss.anim.Spritesheet
import ai.arcblroth.boss.backend.EventLoop
import ai.arcblroth.boss.load.LoadProcess
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import ai.arcblroth.boss.util.ResourceLoader
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

class LevelEngine(eventLoop: EventLoop, private val load: LoadProcess) : Engine {

    private val mesh: Mesh
    private val animation: AnimationController
    private val testingRoom: Mesh

    init {
        val resourceFactory = eventLoop.renderer
        val spritesheet = Spritesheet("assets/entity/polymorph/lago.json", resourceFactory)
        this.mesh = resourceFactory.createMesh(
            arrayOf(
                Vertex(Vector3f(1.0f, 0.25f, -0.25f), Vector4f(16.0f / 48.0f, 0.0f, 0.0f, 0.0f)),
                Vertex(Vector3f(1.0f, 0.25f, 0.25f), Vector4f(0.0f, 0.0f, 0.0f, 0.0f)),
                Vertex(Vector3f(1.0f, -0.25f, 0.25f), Vector4f(0.0f, 16.0f / 48.0f, 0.0f, 0.0f)),
                Vertex(Vector3f(1.0f, -0.25f, -0.25f), Vector4f(16.0f / 48.0f, 16.0f / 48.0f, 0.0f, 0.0f)),
            ),
            intArrayOf(0, 1, 2, 0, 2, 3),
            VertexType.TEX1,
            spritesheet.texture,
            null
        )
        this.animation = AnimationController(mesh, OffsetIndex.FIRST, spritesheet, 2..7, true)

        this.testingRoom = resourceFactory.createMeshFromVox(
            ResourceLoader.loadResourceAsBytes("assets/room/testing.vox")
        )
        this.testingRoom.transform = Matrix4f().translation(50.0f, -50.0f, 40.0f)
    }

    override fun step(eventLoop: EventLoop, lastFrameTime: Long): Pair<Scene, Engine> {
        this.animation.animate()
        val scene = Scene(sceneMeshes = arrayListOf(this.testingRoom, this.animation.mesh))
        return Pair(scene, this)
    }
}
