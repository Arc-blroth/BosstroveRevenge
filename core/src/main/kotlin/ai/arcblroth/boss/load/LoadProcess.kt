package ai.arcblroth.boss.load

import ai.arcblroth.boss.Renderer
import ai.arcblroth.boss.anim.AnimationController
import ai.arcblroth.boss.anim.OffsetIndex
import ai.arcblroth.boss.anim.Spritesheet
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import org.joml.Vector3f
import org.joml.Vector4f
/**
 * LoadProcess handles all of the work needed for
 * initial startup of the game. It locates
 * and registers all of the game's mods, assets,
 * and levels.
 */
class LoadProcess {

    var isDone = false
        private set

    fun load(renderer: Renderer) {
        val spritesheet = Spritesheet("assets/entity/polymorph/lago.json", renderer)
        val mesh = renderer.createMesh(
            arrayOf(
                Vertex(Vector3f(-1.0f, 0.25f, -0.25f), Vector4f(16.0f / 48.0f, 0.0f, 0.0f, 0.0f)),
                Vertex(Vector3f(-1.0f, 0.25f, 0.25f), Vector4f(0.0f, 0.0f, 0.0f, 0.0f)),
                Vertex(Vector3f(-1.0f, -0.25f, 0.25f), Vector4f(0.0f, 16.0f / 48.0f, 0.0f, 0.0f)),
                Vertex(Vector3f(-1.0f, -0.25f, -0.25f), Vector4f(16.0f / 48.0f, 16.0f / 48.0f, 0.0f, 0.0f)),
            ),
            intArrayOf(0, 1, 2, 0, 2, 3),
            VertexType.TEX1,
            spritesheet.texture,
            null
        )
        val animation = AnimationController(mesh, OffsetIndex.FIRST, spritesheet, 2..7, true)
        isDone = true
    }
}
