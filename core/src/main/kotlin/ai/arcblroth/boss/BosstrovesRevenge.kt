package ai.arcblroth.boss

import ai.arcblroth.boss.render.Scene
import java.util.logging.Logger

/**
 * The core class that handles all game state.
 * This takes care of running the game loop, polling input,
 * and requesting rendering.
 */
class BosstrovesRevenge(val backend: Backend) : Runnable {

    val logger: Logger = Logger.getLogger("BosstrovesRevenge")

    init {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1\$tT][%3\$s/%4\$s]: %5\$s %6\$s%n")
    }

    override fun run() {
        println("Hello Bosstrove!")

        val scene = Scene()

        backend.init("Bosstrove's Revenge", "0.1.0")
        backend.runEventLoop {
            render(scene)
        }
    }
}
