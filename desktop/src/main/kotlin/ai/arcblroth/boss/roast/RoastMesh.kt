package ai.arcblroth.boss.roast

import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.VertexType
import ai.arcblroth.boss.roast.lib.ForeignMat4
import ai.arcblroth.boss.roast.lib.ForeignOption_ForeignVec4
import ai.arcblroth.boss.roast.lib.ForeignOption_TextureId
import ai.arcblroth.boss.roast.lib.ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId
import ai.arcblroth.boss.roast.lib.ForeignPair_Vec2__Vec2
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_ForeignMat4
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_ForeignOption_ForeignVec4
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_ForeignPair_Vec2__Vec2
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_f32
import ai.arcblroth.boss.roast.lib.ForeignVec4
import ai.arcblroth.boss.roast.lib.Roast.None_ForeignVec4
import ai.arcblroth.boss.roast.lib.Roast.None_TextureId
import ai.arcblroth.boss.roast.lib.Roast.Some_ForeignVec4
import ai.arcblroth.boss.roast.lib.Roast.Some_TextureId
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_get_opacity
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_get_overlay_color
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_get_texture_offsets
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_get_textures
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_get_transform
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_get_vertex_type
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_set_opacity
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_set_overlay_color
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_set_texture_offsets
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_set_textures
import ai.arcblroth.boss.roast.lib.Roast.roast_mesh_set_transform
import ai.arcblroth.boss.roast.lib.Vec2
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f

class RoastMesh internal constructor(internal val pointer: Long) : Mesh() {
    override val vertexType: VertexType
        get() = ResourceScope.newConfinedScope().use { scope ->
            VertexType.values()[
                roast_mesh_get_vertex_type(scope, this.pointer).unwrapU32()
            ]
        }

    override var textures: Pair<Texture?, Texture?>
        get() = ResourceScope.newConfinedScope().use { scope ->
            val texturesC = roast_mesh_get_textures(scope, this.pointer).unwrap(
                ForeignRoastResult_ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId::`tag$get`,
                ForeignRoastResult_ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId::`ok$slice`,
                ForeignRoastResult_ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId::`err$slice`,
            )
            fun MemorySegment.unwrapTexture(): RoastTexture? =
                if (ForeignOption_TextureId.`tag$get`(this) == Some_TextureId()) {
                    RoastTexture(ForeignOption_TextureId.`some$get`(this))
                } else {
                    null
                }
            val texture0 = ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId.`_0$slice`(texturesC).unwrapTexture()
            val texture1 = ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId.`_1$slice`(texturesC).unwrapTexture()
            Pair(texture0, texture1)
        }
        set(value) = ResourceScope.newConfinedScope().use { scope ->
            val (texture0, texture1) = value
            require(texture0 is RoastTexture? && texture1 is RoastTexture?) {
                "RoastBackend only supports RoastTexture"
            }
            fun RoastTexture?.copyToForeignOptionTextureId(segment: MemorySegment) =
                if (this != null) {
                    ForeignOption_TextureId.`tag$set`(segment, Some_TextureId())
                    ForeignOption_TextureId.`some$set`(segment, this@copyToForeignOptionTextureId.pointer)
                } else {
                    ForeignOption_TextureId.`tag$set`(segment, None_TextureId())
                }
            val pairC = ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId.allocate(scope).apply {
                ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId.`_0$slice`(this).apply {
                    texture0.copyToForeignOptionTextureId(this)
                }
                ForeignPair_ForeignOption_TextureId_____ForeignOption_TextureId.`_1$slice`(this).apply {
                    texture1.copyToForeignOptionTextureId(this)
                }
            }
            roast_mesh_set_textures(scope, this.pointer, pairC).unwrapNothing()
        }

    override var transform: Matrix4f
        get() = ResourceScope.newConfinedScope().use { scope ->
            val transformC = roast_mesh_get_transform(scope, this.pointer).unwrap(
                ForeignRoastResult_ForeignMat4::`tag$get`,
                ForeignRoastResult_ForeignMat4::`ok$slice`,
                ForeignRoastResult_ForeignMat4::`err$slice`,
            )
            // ForeignMat4 should have the same layout as a float[16]
            Matrix4f(transformC.asByteBuffer().asFloatBuffer())
        }
        set(value) = ResourceScope.newConfinedScope().use { scope ->
            val transformC = ForeignMat4.allocate(scope)
            value.get(transformC.asByteBuffer().asFloatBuffer())
            roast_mesh_set_transform(scope, this.pointer, transformC).unwrapNothing()
        }

    override var textureOffsets: Pair<Vector2f, Vector2f>
        get() = ResourceScope.newConfinedScope().use { scope ->
            val offsetsC = roast_mesh_get_texture_offsets(scope, this.pointer).unwrap(
                ForeignRoastResult_ForeignPair_Vec2__Vec2::`tag$get`,
                ForeignRoastResult_ForeignPair_Vec2__Vec2::`ok$slice`,
                ForeignRoastResult_ForeignPair_Vec2__Vec2::`err$slice`,
            )
            fun MemorySegment.toTextureOffset(): Vector2f = Vector2f(
                Vec2.`x$get`(this),
                Vec2.`y$get`(this),
            )
            val offset0C = ForeignPair_Vec2__Vec2.`_0$slice`(offsetsC).toTextureOffset()
            val offset1C = ForeignPair_Vec2__Vec2.`_1$slice`(offsetsC).toTextureOffset()
            Pair(offset0C, offset1C)
        }
        set(value) = ResourceScope.newConfinedScope().use { scope ->
            val pair = ForeignPair_Vec2__Vec2.allocate(scope).apply {
                ForeignPair_Vec2__Vec2.`_0$slice`(this).apply {
                    Vec2.`x$set`(this, value.first.x)
                    Vec2.`y$set`(this, value.first.y)
                }
                ForeignPair_Vec2__Vec2.`_1$slice`(this).apply {
                    Vec2.`x$set`(this, value.second.x)
                    Vec2.`y$set`(this, value.second.y)
                }
            }
            roast_mesh_set_texture_offsets(scope, this.pointer, pair).unwrapNothing()
        }

    override var overlayColor: Vector4f?
        get() = ResourceScope.newConfinedScope().use { scope ->
            val overlayColorC = roast_mesh_get_overlay_color(scope, this.pointer).unwrap(
                ForeignRoastResult_ForeignOption_ForeignVec4::`tag$get`,
                ForeignRoastResult_ForeignOption_ForeignVec4::`ok$slice`,
                ForeignRoastResult_ForeignOption_ForeignVec4::`err$slice`,
            )
            if (ForeignOption_ForeignVec4.`tag$get`(overlayColorC) == Some_ForeignVec4()) {
                ForeignOption_ForeignVec4.`some$slice`(overlayColorC).run {
                    Vector4f(
                        ForeignVec4.`x$get`(this),
                        ForeignVec4.`y$get`(this),
                        ForeignVec4.`z$get`(this),
                        ForeignVec4.`w$get`(this),
                    )
                }
            } else {
                null
            }
        }
        set(value) = ResourceScope.newConfinedScope().use { scope ->
            val overlayColorC = if (value != null) {
                ForeignOption_ForeignVec4.allocate(scope).apply {
                    ForeignOption_ForeignVec4.`tag$set`(this, Some_ForeignVec4())
                    ForeignOption_ForeignVec4.`some$slice`(this).apply {
                        ForeignVec4.`x$set`(this, value.x)
                        ForeignVec4.`y$set`(this, value.y)
                        ForeignVec4.`w$set`(this, value.z)
                        ForeignVec4.`z$set`(this, value.w)
                    }
                }
            } else {
                ForeignOption_ForeignVec4.allocate(scope).apply {
                    ForeignOption_ForeignVec4.`tag$set`(this, None_ForeignVec4())
                }
            }
            roast_mesh_set_overlay_color(scope, this.pointer, overlayColorC).unwrapNothing()
        }

    override var opacity: Float
        get() = ResourceScope.newConfinedScope().use { scope ->
            roast_mesh_get_opacity(scope, this.pointer).unwrap(
                ForeignRoastResult_f32::`tag$get`,
                ForeignRoastResult_f32::`ok$get`,
                ForeignRoastResult_f32::`err$slice`,
            )
        }
        set(value) = ResourceScope.newConfinedScope().use { scope ->
            roast_mesh_set_opacity(scope, this.pointer, value).unwrapNothing()
        }
}
