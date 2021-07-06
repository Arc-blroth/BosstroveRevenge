package ai.arcblroth.boss.load

import ai.arcblroth.boss.Engine
import ai.arcblroth.boss.backend.EventLoop
import ai.arcblroth.boss.backend.ui.Bounds
import ai.arcblroth.boss.backend.ui.Label
import ai.arcblroth.boss.level.LevelEngine
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import ai.arcblroth.boss.util.Color
import ai.arcblroth.boss.util.ResourceLoader
import ai.arcblroth.boss.util.asVector4f
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4d
import org.joml.Vector4f
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

// Padding height and logo colors from
// https://github.com/Arc-blroth/BosstroveRevenge/blob/try1/core/src/main/java/ai/arcblroth/boss/load/LoadEngine.java#L20
// Note that a single "row" of characters in a `PixelAndTextGrid`
// took up 16 pixels and was represented by 2 units of height.
private const val ARBITRARY_PADDING_HEIGHT = 8.0f * 16
private val SAT_BLUE = Color(41, 166, 255).toHSV()
private val LIGHT_BLUE = Color(107, 190, 250).toHSV()

/**
 * The old Bosstrove's Revenge ran at 30 FPS. We keep the
 * logo glow animation at the same speed as in the original
 * and step the animation by [LOGO_ANIMATION_SPEED] every
 * this amount of milliseconds.
 */
private const val MS_PER_STEP = 1000.0 / 30.0

/**
 * Every [MS_PER_STEP] the logo glow animation
 * is incremented by this amount.
 */
private const val LOGO_ANIMATION_SPEED = 0.01

/**
 * LoadEngine is the first engine used in a normal
 * run of Bosstroves' Revenge. It spawns an
 * async [LoadProcess] to do the heavy lifting and
 * responds to requests to register things.
 */
class LoadEngine : Engine {

    private val sendTexture = Channel<TextureCreationParams>(Channel.UNLIMITED)
    private val receiveTexture = Channel<Texture>(Channel.UNLIMITED)
    private val sendMesh = Channel<MeshCreationParams>(Channel.UNLIMITED)
    private val receiveMesh = Channel<Mesh>(Channel.UNLIMITED)
    private val sendMeshVox = Channel<ByteArray>(Channel.UNLIMITED)
    private val receiveMeshVox = Channel<Mesh>(Channel.UNLIMITED)
    private val sendMeshGeometry = Channel<Mesh>(Channel.UNLIMITED)
    private val receiveMeshGeometry = Channel<Mesh>(Channel.UNLIMITED)

    private val loadRenderer = LoadRendererResourceFactory(
        sendTexture,
        receiveTexture,
        sendMesh,
        receiveMesh,
        sendMeshVox,
        receiveMeshVox,
        sendMeshGeometry,
        receiveMeshGeometry
    )

    private val loadProcess = LoadProcess()
    private val loadThreadError = AtomicReference<Throwable>()

    // The same vectors are used to store every blue interpolation
    // calculation to prevent reallocating memory every frame.
    private var interpVec = Vector4d()
    private var colorVec = Vector4f()
    private var transformMat = Matrix4f()

    private var logoTexture: Texture? = null
    private var logoMesh: Mesh? = null
    private var scene: Scene? = null

    init {
        // Start the loading thread
        Thread({ loadProcess.load(loadRenderer) }, "LoadEngine").run {
            setUncaughtExceptionHandler { _, error ->
                loadThreadError.set(error)
            }
            start()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun step(eventLoop: EventLoop, lastFrameTime: Long): Pair<Scene, Engine> {
        // While loading, we try to update the UI at around 60 FPS
        // so that the UI doesn't freeze until loading completes.
        fun exceededFrameRate() = (System.nanoTime() - lastFrameTime).toDouble() > 1e+9 / 60.0

        if (this.scene == null) {
            val logoTexture = eventLoop.getRenderer().createTexture(
                ResourceLoader.loadResourceAsBytes("assets/bitmap.png"),
                TextureSampling.PIXEL,
                true
            )

            val halfWidth: Float = logoTexture.width.toFloat() * 8.0f / 2.0f
            val halfHeight: Float = logoTexture.height.toFloat() * 8.0f / 2.0f

            val logoMesh = eventLoop.getRenderer().createMesh(
                arrayOf(
                    Vertex(Vector3f(halfWidth, halfHeight, 0.0f), Vector4f(1.0f, 1.0f, 0.0f, 0.0f)),
                    Vertex(Vector3f(halfWidth, -halfHeight, 0.0f), Vector4f(1.0f, 0.0f, 0.0f, 0.0f)),
                    Vertex(Vector3f(-halfWidth, -halfHeight, 0.0f), Vector4f(0.0f, 0.0f, 0.0f, 0.0f)),
                    Vertex(Vector3f(-halfWidth, halfHeight, 0.0f), Vector4f(0.0f, 1.0f, 0.0f, 0.0f)),
                ),
                intArrayOf(0, 1, 2, 0, 2, 3),
                VertexType.TEX1,
                logoTexture,
                null
            )

            this.logoTexture = logoTexture
            this.logoMesh = logoMesh

            this.scene = Scene(guiMeshes = arrayListOf(logoMesh))
        }

        val rendererSize = eventLoop.getRenderer().getSize()

        this.logoMesh!!.let {
            // Make the logo change color!
            val blueInterpolation = abs(System.currentTimeMillis() / (MS_PER_STEP / LOGO_ANIMATION_SPEED) % 2 - 1)
            it.overlayColor = Color.fromHSV(
                SAT_BLUE.lerp(LIGHT_BLUE, blueInterpolation, interpVec)
            ).asVector4f(colorVec)

            it.transform = transformMat.translation(
                (rendererSize.x / 2.0).toFloat(),
                (rendererSize.y / 2.0).toFloat() - ARBITRARY_PADDING_HEIGHT / 2.0f,
                0.0f
            )
        }

        // Show the loading text
        val textY = (rendererSize.y / 2.0).toFloat() + (logoTexture!!.height * 8.0f + ARBITRARY_PADDING_HEIGHT) / 2.0f - 16.0f
        eventLoop.getRenderer().showUI {
            window("Test") {
                label(Label(text = "It works!", textColor = ai.arcblroth.boss.util.RED))
            }
            area("loading", Bounds(0.0f, textY, rendererSize.x.toFloat(), rendererSize.y.toFloat())) {
                verticalCenteredJustified {
                    label(Label(text = "Loading - 0%", textColor = Color(40, 237, 63)))
                }
            }
        }

        // Check if the loading thread crashed
        val error = loadThreadError.get()
        if (error != null) {
            throw error
        }

        // Handle requests to register resources.
        while (!exceededFrameRate() && !sendTexture.isEmpty) {
            val params = sendTexture.tryReceive().getOrThrow()
            val texture = eventLoop.getRenderer().createTexture(params.image, params.sampling, params.generateMipmaps)
            receiveTexture.trySendBlocking(texture).getOrThrow()
        }
        while (!exceededFrameRate() && !sendMesh.isEmpty) {
            val params = sendMesh.tryReceive().getOrThrow()
            val mesh = eventLoop.getRenderer().createMesh(
                params.vertices,
                params.indices,
                params.vertexType,
                params.texture0,
                params.texture1
            )
            receiveMesh.trySendBlocking(mesh).getOrThrow()
        }
        while (!exceededFrameRate() && !sendMeshVox.isEmpty) {
            val vox = sendMeshVox.tryReceive().getOrThrow()
            val mesh = eventLoop.getRenderer().createMeshFromVox(vox)
            receiveMeshVox.trySendBlocking(mesh).getOrThrow()
        }
        while (!exceededFrameRate() && !sendMeshGeometry.isEmpty) {
            val geometry = sendMeshGeometry.tryReceive().getOrThrow()
            val mesh = eventLoop.getRenderer().createMeshWithGeometry(geometry)
            receiveMeshGeometry.trySendBlocking(mesh).getOrThrow()
        }

        return if (loadProcess.isDone) {
            Pair(this.scene!!, LevelEngine(loadProcess))
        } else {
            Pair(this.scene!!, this)
        }
    }
}
