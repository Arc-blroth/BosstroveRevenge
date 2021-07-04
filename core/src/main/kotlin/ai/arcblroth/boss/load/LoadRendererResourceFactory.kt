package ai.arcblroth.boss.load

import ai.arcblroth.boss.backend.Renderer
import ai.arcblroth.boss.backend.RendererResourceFactory
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

/**
 * LoadRenderer is a special implementation of [RendererResourceFactory]
 * that allows creating textures and meshes from coroutines
 * other than the main thread. Note that both [createTexture]
 * and [createMesh] will block until creation is complete.
 */
class LoadRendererResourceFactory(
    private val sendTexture: SendChannel<TextureCreationParams>,
    private val receiveTexture: ReceiveChannel<Texture>,
    private val sendMesh: SendChannel<MeshCreationParams>,
    private val receiveMesh: ReceiveChannel<Mesh>,
    private val sendMeshGeometry: SendChannel<Mesh>,
    private val receiveMeshGeometry: ReceiveChannel<Mesh>,
) : RendererResourceFactory {

    override fun createTexture(image: ByteArray, sampling: TextureSampling, generateMipmaps: Boolean): Texture {
        return runBlocking {
            sendTexture.send(TextureCreationParams(image, sampling, generateMipmaps))
            receiveTexture.receive()
        }
    }

    override fun createMesh(
        vertices: Array<Vertex>,
        indices: IntArray,
        vertexType: VertexType,
        texture0: Texture?,
        texture1: Texture?
    ): Mesh {
        return runBlocking {
            sendMesh.send(MeshCreationParams(vertices, indices, vertexType, texture0, texture1))
            receiveMesh.receive()
        }
    }

    override fun createMeshWithGeometry(geometry: Mesh): Mesh {
        return runBlocking {
            sendMeshGeometry.send(geometry)
            receiveMeshGeometry.receive()
        }
    }
}

/**
 * The parameters to [Renderer.createTexture] packaged as a class.
 */
class TextureCreationParams(val image: ByteArray, val sampling: TextureSampling, val generateMipmaps: Boolean)

/**
 * The parameters to [Renderer.createMesh] packaged as a class.
 */
class MeshCreationParams(
    val vertices: Array<Vertex>,
    val indices: IntArray,
    val vertexType: VertexType,
    val texture0: Texture?,
    val texture1: Texture?
)
