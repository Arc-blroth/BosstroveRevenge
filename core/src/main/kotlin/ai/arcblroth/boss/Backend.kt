package ai.arcblroth.boss

import ai.arcblroth.boss.render.Scene

/**
 * Settings for backend renderer initialization. Depending on the backend,
 * some settings may be ignored.
 */
data class RendererSettings(
    /**
     * Size of the renderer surface (not necessarily its swapchain size),
     * relative to the size of the parent display space.
     */
    val rendererSize: Pair<Double, Double> = Pair(1.0, 1.0),

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
     * Renders the scene. This **must not**
     * mutate the scene.
     * @param [scene] Scene to be rendered.
     */
    fun render(scene: Scene)

    /**
     * Requests the event loop to stop after this frame.
     * This will also clean up any resources associated
     * with the backend.
     */
    fun exit()
}
