package ai.arcblroth.boss.render

/**
 * Sampling method for a texture.
 */
enum class TextureSampling {
    /**
     * A linear sampling method that
     * interpolates nearby pixels.
     */
    SMOOTH,
    /**
     * A nearest sampling method that
     * uses the nearest pixel.
     */
    PIXEL,
}

/**
 * A sampled and possibly mipmapped image
 * that can be used alongside a [Mesh].
 */
abstract class Texture {
    /**
     * The sampling method for this texture.
     */
    abstract val sampling: TextureSampling

    /**
     * Whether this texture has mipmaps.
     */
    abstract val mipmapped: Boolean
}
