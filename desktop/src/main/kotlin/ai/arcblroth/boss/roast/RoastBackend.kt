package ai.arcblroth.boss.roast

import ai.arcblroth.boss.backend.Backend
import ai.arcblroth.boss.backend.EventLoop
import ai.arcblroth.boss.backend.Renderer
import ai.arcblroth.boss.backend.RendererSettings
import ai.arcblroth.boss.backend.ui.UI
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import org.joml.Matrix4f
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector4f
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

    external override fun createTexture(image: ByteArray, sampling: TextureSampling, generateMipmaps: Boolean): Texture

    external override fun createMesh(
        vertices: Array<Vertex>,
        indices: IntArray,
        vertexType: VertexType,
        texture0: Texture?,
        texture1: Texture?
    ): Mesh

    external override fun createMeshWithGeometry(geometry: Mesh): Mesh

    external override fun getSize(): Vector2d

    override fun showUI(withUI: UI.() -> Unit) {
        withUI(RoastUI(pointer))
    }

    external override fun render(scene: Scene)

    external override fun exit()
}

/**
 * If the backend `panic!`s or if anything else goes wrong,
 * this exception will be thrown.
 */
class RoastException(msg: String) : RuntimeException(msg)

class RoastTexture private constructor(private val pointer: Long) : Texture() {
    override val width: Int
        external get

    override val height: Int
        external get

    override val sampling: TextureSampling
        external get

    override val mipmapped: Boolean
        external get
}

class RoastMesh private constructor(private val pointer: Long) : Mesh() {
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
