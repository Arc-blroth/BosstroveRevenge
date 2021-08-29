package ai.arcblroth.boss.render

import ai.arcblroth.boss.backend.Renderer
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * The vertex primitive passed to the backend.
 * This can specify a position and one of
 * - 1 color
 * - 2 texture coordinates
 */
class Vertex(val pos: Vector3f, val colorTex: Vector4f)

/**
 * Specifies how to interpret the vertex data used in a [Mesh].
 */
enum class VertexType {
    /**
     * [Vertex.colorTex] holds a single color.
     */
    COLOR,
    /**
     * [Vertex.colorTex] holds one texture coordinate.
     */
    TEX1,
    /**
     * [Vertex.colorTex] holds two texture coordinates.
     */
    TEX2,
}

/**
 * The type of index buffers in a [Mesh].
 */
typealias Index = Int

/**
 * A `Mesh` consists of the geometry and associated textures
 * needed to render a mesh, as well as its model matrix,
 * texture offsets, an optional overlay color, and its opacity.
 *
 * To create a Mesh, use [Renderer.createMesh]
 */
abstract class Mesh {
    abstract val vertexType: VertexType
    abstract var textures: Pair<Texture?, Texture?>
    abstract var transform: Matrix4f
    abstract var textureOffsets: Pair<Vector2f, Vector2f>
    abstract var overlayColor: Vector4f?
    abstract var opacity: Float
}
