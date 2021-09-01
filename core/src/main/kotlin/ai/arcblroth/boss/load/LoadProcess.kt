package ai.arcblroth.boss.load

/**
 * LoadProcess handles all of the work needed for
 * initial startup of the game. It locates
 * and registers all of the game's mods, assets,
 * and levels.
 */
class LoadProcess {

    var isDone = false
        private set

    fun load() {
        isDone = true
    }
}
