package ai.arcblroth.boss

import ai.arcblroth.boss.math.Vector3f
import ai.arcblroth.boss.math.Vector4f
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
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
            init("Bosstrove's Revenge", "0.1.0", RendererSettings(rendererSize = Pair(0.75, 0.75)))

            val scene = Scene()
            var initYet = false
            var lastFrameTime = System.nanoTime()

            runEventLoop {
                if (!initYet) {
                    scene.sceneMeshes.add(
                        getRenderer().createMesh(
                            arrayOf(
                                Vertex(Vector3f(-1.0f, -0.25f, -0.5f), Vector4f(0.0f, 1.0f, 1.0f, 1.0f)),
                                Vertex(Vector3f(-1.0f, 0.25f, 0.0f), Vector4f(0.0f, 1.0f, 0.5f, 1.0f)),
                                Vertex(Vector3f(-1.0f, -0.25f, 0.5f), Vector4f(1.0f, 0.5f, 1.0f, 1.0f)),
                            ),
                            arrayOf(1, 2, 3),
                            VertexType.COLOR,
                            null,
                            null
                        )
                    )
                    initYet = true
                }

                getRenderer().render(scene)

                val currentTime = System.nanoTime()
                logger.info("FPS: ${1e+9f / (currentTime - lastFrameTime).toFloat()}")
                lastFrameTime = currentTime
            }
        }
    }
}
