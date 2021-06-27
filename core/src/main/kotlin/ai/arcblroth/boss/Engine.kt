package ai.arcblroth.boss

import ai.arcblroth.boss.backend.EventLoop
import ai.arcblroth.boss.render.Scene

/**
 * An Engine handles all of the logic
 * that occurs each frame.
 */
interface Engine {

    /**
     * Executes the logic for a single frame and returns
     * a [Scene] to render and the [Engine] to invoke
     * on the next frame. If the returned engine is not
     * this engine, then the running engine will be swapped
     * next frame.
     */
    fun step(eventLoop: EventLoop, lastFrameTime: Long): Pair<Scene, Engine>
}
