package ai.arcblroth.boss.backend.ui

import ai.arcblroth.boss.util.Color
import ai.arcblroth.boss.util.TRANSPARENT

/**
 * A text label widget.
 */
data class Label(
    val text: String,
    val wrap: Boolean? = null,
    val textStyle: TextStyle? = null,
    val backgroundColor: Color = TRANSPARENT,
    val textColor: Color? = null,
    val code: Boolean = false,
    val strong: Boolean = false,
    val weak: Boolean = false,
    val strikethrough: Boolean = false,
    val underline: Boolean = false,
    val italics: Boolean = false,
    val raised: Boolean = false,
)
