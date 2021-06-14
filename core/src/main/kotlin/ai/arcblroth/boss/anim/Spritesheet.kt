package ai.arcblroth.boss.anim

import ai.arcblroth.boss.Renderer
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.util.ResourceLoader
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.joml.Vector2f

/**
 * An arbitrary epsilon value that is added to each texture
 * coordinate offset to prevent a single line of pixels
 * from neighboring frames from showing up.
 */
private const val TEXTURE_OFFSET_EPSILON = 0.00001f

/**
 * A spritesheet containing a set of pixel sprites.
 */
class Spritesheet {

    companion object {
        @JvmStatic
        private val MAPPER = ObjectMapper()
    }

    val width: Int
    val height: Int
    val texture: Texture
    val frames: List<Frame>

    /**
     * Constructs a Spritesheet with the given width, height, backing texture,
     * and list of frames.
     */
    // Manually written constructors? In Kotlin!?
    constructor(width: Int, height: Int, texture: Texture, frames: List<Frame>) {
        this.width = width
        this.height = height
        this.texture = texture
        this.frames = frames
    }

    /**
     * Constructs a Spritesheet from the given spritesheet meta json file, which
     * will be loaded using [ResourceLoader.loadResourceAsBytes].
     */
    constructor(spritesheetMetaPath: String, renderer: Renderer) {
        val serialized = MAPPER.readValue(ResourceLoader.loadResourceAsBytes(spritesheetMetaPath), SerializedSpritesheet::class.java)

        this.width = serialized.size.w
        this.height = serialized.size.h
        this.frames = serialized.frames

        val lastSlash = spritesheetMetaPath.replace('\\', '/').lastIndexOf('/')
        val imagePath = if (lastSlash == -1) {
            serialized.image
        } else {
            spritesheetMetaPath.substring(0, lastSlash) + "/" + serialized.image
        }
        this.texture = renderer.createTexture(
            ResourceLoader.loadResourceAsBytes(imagePath),
            TextureSampling.PIXEL,
            true
        )
    }

    /**
     * Gets the texture offsets into the texture backing this spritesheet for a given frame.
     */
    fun getTextureOffsets(frameIndex: Int): Vector2f {
        val frame = this.frames[frameIndex]
        return Vector2f(
            frame.x.toFloat() / this.width.toFloat() + TEXTURE_OFFSET_EPSILON,
            frame.y.toFloat() / this.height.toFloat() + TEXTURE_OFFSET_EPSILON
        )
    }
}

/**
 * A single frame in a spritesheet.
 */
data class Frame(
    @JsonProperty("x") val x: Int,
    @JsonProperty("y") val y: Int,
    @JsonProperty("w") val w: Int,
    @JsonProperty("h") val h: Int,
    @JsonProperty("duration") val duration: Long
)

/**
 * Serialized form of a spritesheet as outputted by [ai.arcblroth.boss.gradle.AsepritePlugin].
 */
private data class SerializedSpritesheet(
    @JsonProperty("frames") val frames: List<Frame>,
    @JsonProperty("image") val image: String,
    @JsonProperty("size") val size: Size
) {
    data class Size(
        @JsonProperty("w") val w: Int,
        @JsonProperty("h") val h: Int
    )
}
