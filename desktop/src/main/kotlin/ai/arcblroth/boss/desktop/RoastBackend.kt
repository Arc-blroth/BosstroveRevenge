package ai.arcblroth.boss.desktop

import ai.arcblroth.boss.Backend
import ai.arcblroth.boss.EventLoop
import ai.arcblroth.boss.RendererSettings
import ai.arcblroth.boss.render.Scene
import org.scijava.nativelib.NativeLoader

/**
 * The winit + vulkano backend, implemented in Rust because
 * I need to improve my Rust skills and don't wanna import
 * all of LWJGL
 *
 * Called "Roast" as a reference to the Bosstrove's
 * superior roasting skills.
 */
class RoastBackend : Backend, EventLoop {
    companion object {
        init {
            NativeLoader.loadLibrary("roast")
        }
    }

    external override fun init(appName: String, appVersion: String, rendererSettings: RendererSettings)

    external override fun runEventLoop(step: EventLoop.() -> Unit)

    external override fun render(scene: Scene)

    external override fun exit()
}
