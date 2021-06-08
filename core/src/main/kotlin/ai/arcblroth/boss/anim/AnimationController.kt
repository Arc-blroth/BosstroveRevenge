package ai.arcblroth.boss.anim

import ai.arcblroth.boss.render.Mesh

enum class OffsetIndex {
    FIRST,
    SECOND
}

/**
 * A helper controller that updates a mesh's texture offsets
 * over time to display a animation.
 *
 * @param mesh Mesh to animate.
 * @param whichOffset Which one of the mesh's texture offsets to animate.
 * @param spritesheet Backing spritesheet to grab frames from.
 * @param frames A list of frame indices that will be animated in order.
 * @param loop Whether or not to continuously loop the animation.
 */
class AnimationController(val mesh: Mesh, val whichOffset: OffsetIndex, val spritesheet: Spritesheet, val frames: List<Int>, val loop: Boolean) {

    private var startTime: Long = System.currentTimeMillis()
    private var currentFrame = 0
    var done = false
        private set

    constructor(mesh: Mesh, whichOffset: OffsetIndex, spritesheet: Spritesheet, frames: IntRange, loop: Boolean) : this(mesh, whichOffset, spritesheet, frames.toList(), loop)

    /**
     * Updates the animation.
     */
    fun animate() {
        if (this.done) return

        val now = System.currentTimeMillis()
        if (now - startTime >= spritesheet.frames[this.frames[currentFrame]].duration) {
            this.startTime = now
            this.currentFrame++
            if (this.currentFrame == this.frames.size) {
                if (this.loop) {
                    this.currentFrame = 0
                } else {
                    this.done = true
                }
            }
        }
        val offsets = spritesheet.getTextureOffsets(this.frames[currentFrame])
        when (this.whichOffset) {
            OffsetIndex.FIRST -> this.mesh.textureOffsets = offsets to this.mesh.textureOffsets.second
            OffsetIndex.SECOND -> this.mesh.textureOffsets = this.mesh.textureOffsets.first to offsets
        }
    }
}
