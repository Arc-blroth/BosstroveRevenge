package ai.arcblroth.boss.roast

import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_TextureSampling
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_bool
import ai.arcblroth.boss.roast.lib.Roast.roast_texture_get_height
import ai.arcblroth.boss.roast.lib.Roast.roast_texture_get_mipmapped
import ai.arcblroth.boss.roast.lib.Roast.roast_texture_get_texture_sampling
import ai.arcblroth.boss.roast.lib.Roast.roast_texture_get_width
import jdk.incubator.foreign.ResourceScope

class RoastTexture internal constructor(internal val pointer: Long) : Texture() {
    override val width: Int
        get() = ResourceScope.newConfinedScope().use { scope ->
            roast_texture_get_width(scope, this.pointer).unwrapU32()
        }

    override val height: Int
        get() = ResourceScope.newConfinedScope().use { scope ->
            roast_texture_get_height(scope, this.pointer).unwrapU32()
        }

    override val sampling: TextureSampling
        get() = ResourceScope.newConfinedScope().use { scope ->
            TextureSampling.values()[
                roast_texture_get_texture_sampling(scope, this.pointer).unwrap(
                    ForeignRoastResult_TextureSampling::`tag$get`,
                    ForeignRoastResult_TextureSampling::`ok$get`,
                    ForeignRoastResult_TextureSampling::`err$slice`,
                ).toInt()
            ]
        }

    override val mipmapped: Boolean
        get() = ResourceScope.newConfinedScope().use { scope ->
            roast_texture_get_mipmapped(scope, this.pointer).unwrap(
                ForeignRoastResult_bool::`tag$get`,
                ForeignRoastResult_bool::`ok$get`,
                ForeignRoastResult_bool::`err$slice`,
            ).fromCBool()
        }
}
