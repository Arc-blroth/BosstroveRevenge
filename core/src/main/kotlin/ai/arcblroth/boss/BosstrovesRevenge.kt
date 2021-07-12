package ai.arcblroth.boss

import ai.arcblroth.boss.backend.Backend
import ai.arcblroth.boss.backend.RendererSettings
import ai.arcblroth.boss.backend.ui.Bounds
import ai.arcblroth.boss.backend.ui.Label
import ai.arcblroth.boss.load.LoadEngine
import ai.arcblroth.boss.util.WHITE
import org.joml.Vector2d
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private const val BYTES_IN_MEGABYTE: Double = (2 shl 20).toDouble()

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

        try {
            backend.run {
                init("Bosstrove's Revenge", "0.1.0", RendererSettings(rendererSize = Vector2d(0.5, 0.5)))

                val fpsProfilingQueue = ArrayDeque<Long>()
                val usedMemProfilingQueue = ArrayDeque<Long>()
                val totalMemProfilingQueue = ArrayDeque<Long>()
                var averageFps = 0.0
                var averageUsedMem = 0.0
                var averageTotalMem = 0.0
                var lastProfileUpdateTime = System.nanoTime()
                var lastFrameTime = lastProfileUpdateTime

                var engine: Engine = LoadEngine()

                runEventLoop {
                    // Step engine
                    val (scene, newEngine) = engine.step(this, lastFrameTime)
                    engine = newEngine

                    // Profiling
                    run {
                        val currentTime = System.nanoTime()

                        // Update the debug profiling every second
                        if (currentTime - lastProfileUpdateTime >= 1e+9) {
                            averageFps = 1e+9 / fpsProfilingQueue.average()
                            averageUsedMem = usedMemProfilingQueue.average() / BYTES_IN_MEGABYTE
                            averageTotalMem = totalMemProfilingQueue.average() / BYTES_IN_MEGABYTE

                            fpsProfilingQueue.clear()
                            usedMemProfilingQueue.clear()
                            totalMemProfilingQueue.clear()

                            lastProfileUpdateTime = currentTime
                        }

                        fpsProfilingQueue.add(currentTime - lastFrameTime)
                        lastFrameTime = currentTime

                        val runtime = Runtime.getRuntime()
                        val totalMemory = runtime.totalMemory()
                        usedMemProfilingQueue.add(totalMemory - runtime.freeMemory())
                        totalMemProfilingQueue.add(totalMemory)
                    }

                    // Show FPS and memory usage for debugging
                    renderer.showUI {
                        val rendererSize = renderer.getSize()
                        val bounds = Bounds(2.0f, 0.0f, rendererSize.x.toFloat() - 4.0f, rendererSize.y.toFloat())
                        area("fps_debug", bounds) {
                            horizontal {
                                label(
                                    Label(
                                        text = String.format("%.0f FPS", averageFps),
                                        textColor = WHITE
                                    )
                                )
                            }
                        }
                        area("mem_debug", bounds) {
                            horizontalRight {
                                label(
                                    Label(
                                        text = String.format("%.0f MB / %.0f MB", averageUsedMem, averageTotalMem),
                                        textColor = WHITE
                                    )
                                )
                            }
                        }
                    }

                    // Render frame
                    renderer.render(scene)
                }
            }
        } catch (e: Throwable) {
            // If we reach here, then the JVM might be
            // in an unstable state, especially when
            // using RoastBackend. Thus we abort the
            // process as soon as possible so that the
            // JVM can be put out of its misery :)
            logger.error("FATAL ERROR", e)
            exitProcess(-1)
        }
    }
}
