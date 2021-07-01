package ai.arcblroth.boss.backend.ui

typealias Contents = Area.() -> Unit

/**
 * Top-level context for an intermediate mode UI.
 *
 * Widgets cannot be added directly to this top
 * level UI. Instead, they must be added to an
 * area defined on this UI.
 */
interface UI {
    /**
     * Adds an [Area] that will take up any remaining
     * space not taken up by the other areas.
     */
    fun centerPanel(addContents: Contents)

    /**
     * Adds an [Area] located at the given position
     * with the given bounds on screen.
     */
    fun area(name: String, bounds: Bounds, addContents: Contents)

    /**
     * Adds an debug window [Area] that may be resizable.
     */
    fun window(name: String, resizable: Boolean = true, addContents: Contents)
}
