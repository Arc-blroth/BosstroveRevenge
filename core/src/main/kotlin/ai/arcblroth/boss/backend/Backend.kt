package ai.arcblroth.boss.backend

import ai.arcblroth.boss.backend.ui.UI
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import org.joml.Vector2d

/**
 * Settings for backend renderer initialization. Depending on the backend,
 * some settings may be ignored.
 */
data class RendererSettings(
    /**
     * Size of the renderer surface (not necessarily its swapchain size),
     * relative to the size of the parent display space.
     */
    val rendererSize: Vector2d = Vector2d(0.0, 0.0),

    /**
     * Fullscreen mode. May not have an effect if fullscreen
     * doesn't exist on the target platform.
     */
    val fullscreenMode: FullscreenMode = FullscreenMode.NONE,

    /**
     * Whether to make the renderer surface transparent.
     */
    val transparent: Boolean = false,
) {
    enum class FullscreenMode {
        NONE, BORDERLESS, EXCLUSIVE
    }
}

/**
 * A `Backend` is responsible for running the main
 * event loop, polling input, and rendering output.
 * This is implemented differently on each platform.
 *
 * **Thread Safety**: The Backend assumes that it will
 * only ever be called from one thread. Do not
 * attempt to invoke Backend or EventLoop methods from
 * any thread other than the one that calls [init] on
 * the Backend, or race conditions may occur.
 */
interface Backend {
    /**
     * Initializes the backend, which may involve creating a surface to draw on
     * and setting up input methods.
     * @param appName Name of this app. Used as the "title" to the surface.
     * @param appVersion Version of the app. Should be a semver string.
     * @param rendererSettings Initial settings used in renderer initialization.
     */
    fun init(appName: String, appVersion: String, rendererSettings: RendererSettings = RendererSettings())

    /**
     * Runs the main event loop and call [step] each frame.
     * **This method will never return**.
     * @param step called once per frame on the event loop thread
     */
    fun runEventLoop(step: EventLoop.() -> Unit): Nothing
}

/**
 * Most backend functions can only be called inside an event loop.
 * To start the event loop, use [Backend.runEventLoop].
 */
interface EventLoop {
    /**
     * Gets the backend [Renderer] for this event loop.
     */
    fun getRenderer(): Renderer

    /**
     * Requests the event loop to stop after this frame.
     * This will also clean up any resources associated
     * with the backend.
     */
    fun exit()
}

/**
 * The backend renderer handles creating and rendering [Meshes][Mesh]
 * and [Textures][Texture]. This interface is separate from [Renderer]
 * so that code that only creates or destroys resources does not need
 * access to a full Renderer implementation.
 */
interface RendererResourceFactory {
    /**
     * Creates a texture that can then be used in a [Mesh].
     * @param [image] Raw source of the texture file. What file formats
     *                are supported are implementation dependent.
     * @param [sampling] Sampling type of the texture.
     * @param [generateMipmaps] Whether to generate and use mipmaps
     *                          for this texture.
     */
    fun createTexture(
        image: ByteArray,
        sampling: TextureSampling,
        generateMipmaps: Boolean,
    ): Texture

    /**
     * Creates a mesh that can be used in a [Scene].
     * @param [vertices] Vertices of the mesh.
     * @param [indices] Indices of the mesh.
     * @param [vertexType] How to interpret each [Vertex] in the [vertices].
     * @param [texture0] First optional texture slot to bind when rendering this mesh.
     * @param [texture1] Second optional texture slot to bind when rendering this mesh.
     */
    fun createMesh(
        vertices: Array<Vertex>,
        indices: IntArray,
        vertexType: VertexType,
        texture0: Texture?,
        texture1: Texture?,
    ): Mesh

    /**
     * Creates a mesh from a MagicaVoxel file.
     * @param [vox] The bytes of the MagicaVoxel file.
     */
    fun createMeshFromVox(vox: ByteArray): Mesh

    /**
     * Creates a mesh with the same geometry as an existing mesh.
     * @param [geometry] The mesh from which to copy the vertices,
     *                   indices, and textures for this mesh.
     */
    fun createMeshWithGeometry(geometry: Mesh): Mesh
}

/**
 * A `Renderer` is responsible for managing renderer
 * resources and rendering each frame of the game.
 */
interface Renderer : RendererResourceFactory {
    /**
     * Gets the size of the output surface of this renderer.
     */
    fun getSize(): Vector2d

    /**
     * Starts building the intermediate mode UI.
     *
     * Note that this can be called multiple times
     * within a frame - all parts of the UI will be
     * rendered once [render] is called.
     * @see UI
     */
    fun showUI(withUI: UI.() -> Unit)

    /**
     * Renders the scene.
     * Implementations of this method **must not**  mutate the scene.
     * @param [scene] Scene to be rendered.
     */
    fun render(scene: Scene)
}
