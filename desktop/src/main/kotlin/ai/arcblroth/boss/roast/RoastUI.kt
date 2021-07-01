package ai.arcblroth.boss.roast

import ai.arcblroth.boss.backend.ui.Area
import ai.arcblroth.boss.backend.ui.Bounds
import ai.arcblroth.boss.backend.ui.Contents
import ai.arcblroth.boss.backend.ui.Label
import ai.arcblroth.boss.backend.ui.UI

class RoastUI internal constructor(private val pointer: Long) : UI {
    external override fun centerPanel(addContents: Contents)

    external override fun area(name: String, bounds: Bounds, addContents: Contents)

    external override fun window(name: String, resizable: Boolean, addContents: Contents)
}

class RoastArea : Area {
    private var pointer: Long = 0

    external override fun horizontal(addContents: Contents)

    external override fun horizontalWrapped(addContents: Contents)

    external override fun vertical(addContents: Contents)

    external override fun verticalCentered(addContents: Contents)

    external override fun verticalCenteredJustified(addContents: Contents)

    external override fun label(label: Label)
}
