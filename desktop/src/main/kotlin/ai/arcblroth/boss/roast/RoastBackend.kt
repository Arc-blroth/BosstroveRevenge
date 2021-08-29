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
import ai.arcblroth.boss.roast.lib.DVec2
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_Nothing
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_u64
import ai.arcblroth.boss.roast.lib.JavaLoggerCallback
import ai.arcblroth.boss.roast.lib.JavaLoggerCallbacks
import ai.arcblroth.boss.roast.lib.Roast.DEFAULT_TEXTURE_NUMBERS_LEN
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_init
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_run_event_loop
import ai.arcblroth.boss.roast.lib.Step
import jdk.incubator.foreign.MemoryCopy
import jdk.incubator.foreign.MemoryLayouts
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import org.joml.Matrix4f
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector4f
import org.scijava.nativelib.NativeLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Random
import ai.arcblroth.boss.roast.lib.RendererSettings as ForeignRendererSettings

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
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger("RoastBackend")

        private val LOGGER_CALLBACKS: MemorySegment
        private val DEFAULT_TEXTURE_NUMBERS: MemorySegment

        init {
            NativeLoader.loadLibrary("roast")

            val scope = ResourceScope.globalScope()
            fun makeCallback(method: Logger.(String) -> Unit) =
                JavaLoggerCallback.allocate({ ptr, len -> LOGGER.method(fromRustString(ptr, len)) }, scope)
            LOGGER_CALLBACKS = JavaLoggerCallbacks.allocate(scope).apply {
                JavaLoggerCallbacks.`error$set`(this, makeCallback(Logger::error))
                JavaLoggerCallbacks.`warn$set`(this, makeCallback(Logger::warn))
                JavaLoggerCallbacks.`info$set`(this, makeCallback(Logger::info))
                JavaLoggerCallbacks.`debug$set`(this, makeCallback(Logger::debug))
                JavaLoggerCallbacks.`trace$set`(this, makeCallback(Logger::trace))
            }

            val random = Random(16)
            val textureNums = DoubleArray(DEFAULT_TEXTURE_NUMBERS_LEN()) {
                random.nextDouble()
            }
            DEFAULT_TEXTURE_NUMBERS = MemorySegment.allocateNative(
                textureNums.size.toLong() * MemoryLayouts.JAVA_DOUBLE.byteSize(),
                scope
            )
            MemoryCopy.copyFromArray(textureNums, 0, textureNums.size, DEFAULT_TEXTURE_NUMBERS, 0)
        }
    }

    /**
     * Internal pointer to the RoastBackend struct.
     */
    private var pointer = 0L

    override fun init(appName: String, appVersion: String, rendererSettings: RendererSettings) {
        ResourceScope.newConfinedScope().use { scope ->
            val appNameC = toRustString(scope, appName)
            val appVersionC = toRustString(scope, appVersion)
            val rendererSettingsC = ForeignRendererSettings.allocate(scope).apply {
                ForeignRendererSettings.`renderer_size$slice`(this).apply {
                    DVec2.`x$set`(this, rendererSettings.rendererSize.x)
                    DVec2.`y$set`(this, rendererSettings.rendererSize.y)
                }
                ForeignRendererSettings.`fullscreen_mode$set`(this, rendererSettings.fullscreenMode.ordinal)
                ForeignRendererSettings.`transparent$set`(this, rendererSettings.transparent.toCBool())
            }
            this.pointer = roast_backend_init(
                scope,
                LOGGER_CALLBACKS,
                DEFAULT_TEXTURE_NUMBERS,
                appNameC.address(),
                appNameC.byteSize(),
                appVersionC.address(),
                appVersionC.byteSize(),
                rendererSettingsC,
            ).unwrap(
                ForeignRoastResult_u64::`tag$get`,
                ForeignRoastResult_u64::`ok$get`,
                ForeignRoastResult_u64::`err$slice`
            )
        }
    }

    override fun runEventLoop(step: EventLoop.() -> Unit): Nothing {
        ResourceScope.newConfinedScope().use { scope ->
            val wrappedStep = wrapUpcall(
                scope,
                ForeignRoastResult_Nothing::allocate,
                ForeignRoastResult_Nothing::`tag$set`,
                ForeignRoastResult_Nothing::`err$slice`,
            ) { this.step() }
            val stepC = Step.allocate(wrappedStep, scope)
            roast_backend_run_event_loop(scope, this.pointer, stepC).unwrap(
                ForeignRoastResult_Nothing::`tag$get`,
                ForeignRoastResult_Nothing::`ok$get`,
                ForeignRoastResult_Nothing::`err$slice`
            )
            throw UNREACHABLE("event loop should never return normally")
        }
    }

    override val renderer: Renderer
        get() = this

    external override fun createTexture(image: ByteArray, sampling: TextureSampling, generateMipmaps: Boolean): Texture

    external override fun createMesh(
        vertices: Array<Vertex>,
        indices: IntArray,
        vertexType: VertexType,
        texture0: Texture?,
        texture1: Texture?
    ): Mesh

    external override fun createMeshFromVox(vox: ByteArray): Mesh

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
