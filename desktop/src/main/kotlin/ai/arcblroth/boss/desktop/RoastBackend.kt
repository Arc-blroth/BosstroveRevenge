package ai.arcblroth.boss.desktop

import ai.arcblroth.boss.Backend
import ai.arcblroth.boss.EventLoop
import ai.arcblroth.boss.Renderer
import ai.arcblroth.boss.RendererSettings
import ai.arcblroth.boss.math.Matrix4f
import ai.arcblroth.boss.math.Vector2f
import ai.arcblroth.boss.math.Vector4f
import ai.arcblroth.boss.render.Index
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import org.scijava.nativelib.NativeLoader
import org.slf4j.LoggerFactory

/**
 * The winit + vulkano backend, implemented in Rust because
 * I need to improve my Rust skills and don't wanna import
 * all of LWJGL
 *
 * Called "Roast" as a reference to the Bosstrove's
 * superior roasting skills.
 */
class RoastBackend : Backend, EventLoop, Renderer {
    companion object {
        init {
            NativeLoader.loadLibrary("roast")
        }

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger("RoastBackend")
    }

    /**
     * Internal pointer to the RoastBackend struct.
     */
    private var pointer = 0L

    external override fun init(appName: String, appVersion: String, rendererSettings: RendererSettings)

    external override fun runEventLoop(step: EventLoop.() -> Unit): Nothing

    override fun getRenderer(): Renderer = this

    external override fun createTexture(image: Array<Byte>, sampling: TextureSampling, generateMipmaps: Boolean): Texture

    external override fun createMesh(
        vertices: Array<Vertex>,
        indices: Array<Index>,
        vertexType: VertexType,
        texture0: Texture?,
        texture1: Texture?
    ): Mesh

    external override fun render(scene: Scene)

    external override fun exit()
}

/**
 * If the backend `panic!`s or if anything else goes wrong,
 * this exception will be thrown.
 */
class RoastException(msg: String) : RuntimeException(msg)

class RoastMesh(
    private val pointer: Long,
) : Mesh() {
    override val vertexType: VertexType
        external get

    override var textures: Pair<Texture?, Texture?>
        external get
        external set

    override var transform: Matrix4f
        external get
        external set

    override var textureOffsets: Pair<Vector2f?, Vector2f?>
        external get
        external set

    override var overlayColor: Vector4f?
        external get
        external set

    override var opacity: Float
        external get
        external set
}
