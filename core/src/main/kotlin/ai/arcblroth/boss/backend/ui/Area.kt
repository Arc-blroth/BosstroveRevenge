package ai.arcblroth.boss.backend.ui

/**
 * The position and size of an [Area].
 */
data class Bounds(val x: Float, val y: Float, val w: Float, val h: Float)

/**
 * A subsection of the UI that can contain widgets.
 */
interface Area {
    // ===================================
    //               Layout
    // ===================================

    /**
     * Add a subsection of this area with
     * a horizontal layout.
     */
    fun horizontal(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a horizontal layout that wraps after
     * reaching the maximum width.
     */
    fun horizontalWrapped(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a left-aligned vertical layout.
     */
    fun vertical(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a centered vertical layout.
     */
    fun verticalCentered(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a centered and justified vertical layout.
     */
    fun verticalCenteredJustified(addContents: Contents)

    // ===================================
    //              Widgets
    // ===================================

    /**
     * Adds a text label to this area.
     */
    fun label(label: Label)
}
