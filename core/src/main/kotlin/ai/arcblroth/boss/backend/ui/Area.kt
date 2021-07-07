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
    @BossUIDsl
    fun horizontal(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a right-to-left horizontal layout.
     */
    @BossUIDsl
    fun horizontalRight(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a horizontal layout that wraps after
     * reaching the maximum width.
     */
    @BossUIDsl
    fun horizontalWrapped(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a left-aligned vertical layout.
     */
    @BossUIDsl
    fun vertical(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a centered vertical layout.
     */
    @BossUIDsl
    fun verticalCentered(addContents: Contents)

    /**
     * Add a subsection of this area with
     * a centered and justified vertical layout.
     */
    @BossUIDsl
    fun verticalCenteredJustified(addContents: Contents)

    // ===================================
    //              Widgets
    // ===================================

    /**
     * Adds a text label to this area.
     */
    @BossUIDsl
    fun label(label: Label)
}
