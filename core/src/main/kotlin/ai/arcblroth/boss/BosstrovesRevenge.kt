package ai.arcblroth.boss

import ai.arcblroth.boss.load.LoadEngine
import org.joml.Vector2d
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The core class that handles all game state.
 * This takes care of running the game loop, polling input,
 * and requesting rendering.
 */
class BosstrovesRevenge(val backend: Backend) : Runnable {

    val logger: Logger = LoggerFactory.getLogger("BosstrovesRevenge")

    init {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1\$tT][%3\$s/%4\$s]: %5\$s%6\$s%n")
    }

    override fun run() {
        logger.info("Hello Bosstrove!")

        backend.run {
            init("Bosstrove's Revenge", "0.1.0", RendererSettings(rendererSize = Vector2d(0.75, 0.75)))

            var lastFrameTime = System.nanoTime()
            var engine: Engine = LoadEngine()

            runEventLoop {
                val (scene, newEngine) = engine.step(this, lastFrameTime)
                engine = newEngine
                getRenderer().render(scene)

                val currentTime = System.nanoTime()
                // logger.info("FPS: ${1e+9f / (currentTime - lastFrameTime).toFloat()}")
                lastFrameTime = currentTime
            }
        }
    }
}
