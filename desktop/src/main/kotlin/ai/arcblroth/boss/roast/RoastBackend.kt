package ai.arcblroth.boss.roast

import ai.arcblroth.boss.backend.Backend
import ai.arcblroth.boss.backend.EventLoop
import ai.arcblroth.boss.backend.Renderer
import ai.arcblroth.boss.backend.RendererSettings
import ai.arcblroth.boss.backend.ui.UI
import ai.arcblroth.boss.render.Mesh
import ai.arcblroth.boss.render.Scene
import ai.arcblroth.boss.render.Texture
import ai.arcblroth.boss.render.TextureSampling
import ai.arcblroth.boss.render.Vertex
import ai.arcblroth.boss.render.VertexType
import ai.arcblroth.boss.roast.lib.DVec2
import ai.arcblroth.boss.roast.lib.DVec3
import ai.arcblroth.boss.roast.lib.ForeignOption_TextureId
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_DVec2
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_Nothing
import ai.arcblroth.boss.roast.lib.JavaLoggerCallback
import ai.arcblroth.boss.roast.lib.JavaLoggerCallbacks
import ai.arcblroth.boss.roast.lib.Roast.DEFAULT_TEXTURE_NUMBERS_LEN
import ai.arcblroth.boss.roast.lib.Roast.None_TextureId
import ai.arcblroth.boss.roast.lib.Roast.Some_TextureId
import ai.arcblroth.boss.roast.lib.Roast.int64_t
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_create_mesh
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_create_mesh_from_vox
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_create_mesh_with_geometry
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_create_texture
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_get_size
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_init
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_render
import ai.arcblroth.boss.roast.lib.Roast.roast_backend_run_event_loop
import ai.arcblroth.boss.roast.lib.Step
import jdk.incubator.foreign.MemoryCopy
import jdk.incubator.foreign.MemoryLayouts
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import jdk.incubator.foreign.SegmentAllocator
import org.joml.Vector2d
import org.scijava.nativelib.NativeLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Random
import ai.arcblroth.boss.roast.lib.Camera as ForeignCamera
import ai.arcblroth.boss.roast.lib.RendererSettings as ForeignRendererSettings
import ai.arcblroth.boss.roast.lib.Vertex as ForeignVertex

/**
 * The winit + vulkano backend, implemented in Rust because
 * I need to improve my Rust skills and don't wanna import
 * all of LWJGL
 *
 * Called "Roast" as a reference to the Bosstrove's
 * superior roasting skills.
 */
class RoastBackend : Backend, EventLoop, Renderer {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger("RoastBackend")

        private val LOGGER_CALLBACKS: MemorySegment
        private val DEFAULT_TEXTURE_NUMBERS: MemorySegment

        init {
            NativeLoader.loadLibrary("roast")

            val scope = ResourceScope.globalScope()
            fun makeCallback(method: Logger.(String) -> Unit) =
                JavaLoggerCallback.allocate({ ptr, len -> LOGGER.method(fromRustString(ptr, len)) }, scope)
            LOGGER_CALLBACKS = JavaLoggerCallbacks.allocate(scope).apply {
                JavaLoggerCallbacks.`error$set`(this, makeCallback(Logger::error))
                JavaLoggerCallbacks.`warn$set`(this, makeCallback(Logger::warn))
                JavaLoggerCallbacks.`info$set`(this, makeCallback(Logger::info))
                JavaLoggerCallbacks.`debug$set`(this, makeCallback(Logger::debug))
                JavaLoggerCallbacks.`trace$set`(this, makeCallback(Logger::trace))
            }

            val random = Random(16)
            val textureNums = DoubleArray(DEFAULT_TEXTURE_NUMBERS_LEN()) {
                random.nextDouble()
            }
            DEFAULT_TEXTURE_NUMBERS = MemorySegment.allocateNative(
                textureNums.size.toLong() * MemoryLayouts.JAVA_DOUBLE.byteSize(),
                scope
            )
            MemoryCopy.copyFromArray(textureNums, 0, textureNums.size, DEFAULT_TEXTURE_NUMBERS, 0)
        }
    }

    /**
     * Internal pointer to the RoastBackend struct.
     */
    private var pointer = 0L

    override fun init(appName: String, appVersion: String, rendererSettings: RendererSettings) {
        ResourceScope.newConfinedScope().use { scope ->
            val appNameC = toRustString(scope, appName)
            val appVersionC = toRustString(scope, appVersion)
            val rendererSettingsC = ForeignRendererSettings.allocate(scope).apply {
                ForeignRendererSettings.`renderer_size$slice`(this).apply {
                    DVec2.`x$set`(this, rendererSettings.rendererSize.x)
                    DVec2.`y$set`(this, rendererSettings.rendererSize.y)
                }
                ForeignRendererSettings.`fullscreen_mode$set`(this, rendererSettings.fullscreenMode.ordinal)
                ForeignRendererSettings.`transparent$set`(this, rendererSettings.transparent.toCBool())
            }
            this.pointer = roast_backend_init(
                scope,
                LOGGER_CALLBACKS,
                DEFAULT_TEXTURE_NUMBERS,
                appNameC.address(),
                appNameC.byteSize(),
                appVersionC.address(),
                appVersionC.byteSize(),
                rendererSettingsC,
            ).unwrapU64()
        }
    }

    override fun runEventLoop(step: EventLoop.() -> Unit): Nothing {
        ResourceScope.newConfinedScope().use { scope ->
            val wrappedStep = wrapUpcall(
                scope,
                ForeignRoastResult_Nothing::allocate,
                ForeignRoastResult_Nothing::`tag$set`,
                ForeignRoastResult_Nothing::`err$slice`,
            ) { this.step() }
            val stepC = Step.allocate(wrappedStep, scope)
            roast_backend_run_event_loop(scope, this.pointer, stepC).unwrapNothing()
            throw UNREACHABLE("event loop should never return normally")
        }
    }

    override val renderer: Renderer
        get() = this

    override fun createTexture(image: ByteArray, sampling: TextureSampling, generateMipmaps: Boolean): Texture {
        ResourceScope.newConfinedScope().use { scope ->
            val imageC = copyToNativeArray(scope, image)
            return RoastTexture(
                roast_backend_create_texture(
                    scope,
                    this.pointer,
                    imageC.address(),
                    image.size.toLong(),
                    sampling.ordinal.toByte(),
                    generateMipmaps.toCBool(),
                ).unwrapU64()
            )
        }
    }

    override fun createMesh(
        vertices: Array<Vertex>,
        indices: IntArray,
        vertexType: VertexType,
        texture0: Texture?,
        texture1: Texture?
    ): Mesh {
        require(texture0 is RoastTexture? && texture1 is RoastTexture?) { "RoastBackend only supports RoastTexture" }
        ResourceScope.newConfinedScope().use { scope ->
            val sizeofVertex = ForeignVertex.sizeof()
            val verticesC = ForeignVertex.allocateArray(vertices.size, scope)
            for ((i, vertex) in vertices.withIndex()) {
                val pos = floatArrayOf(vertex.pos.x, vertex.pos.y, vertex.pos.z)
                val colorTex = floatArrayOf(vertex.colorTex.x, vertex.colorTex.y, vertex.colorTex.z, vertex.colorTex.w)
                verticesC.asSlice(i.toLong() * sizeofVertex, sizeofVertex).apply {
                    MemoryCopy.copyFromArray(pos, 0, pos.size, ForeignVertex.`pos$slice`(this), 0)
                    MemoryCopy.copyFromArray(colorTex, 0, pos.size, ForeignVertex.`color_tex$slice`(this), 0)
                }
            }

            fun Texture?.toNative(scope: ResourceScope): MemorySegment =
                if (this is RoastTexture) {
                    ForeignOption_TextureId.allocate(scope).apply {
                        ForeignOption_TextureId.`tag$set`(this, Some_TextureId())
                        ForeignOption_TextureId.`some$set`(this, this@toNative.pointer)
                    }
                } else {
                    ForeignOption_TextureId.allocate(scope).apply {
                        ForeignOption_TextureId.`tag$set`(this, None_TextureId())
                    }
                }
            val texture0C = texture0.toNative(scope)
            val texture1C = texture1.toNative(scope)

            val indicesC = copyToNativeArray(scope, indices)
            return RoastMesh(
                roast_backend_create_mesh(
                    scope,
                    this.pointer,
                    verticesC.address(),
                    vertices.size.toLong(),
                    indicesC.address(),
                    indices.size.toLong(),
                    vertexType.ordinal,
                    texture0C,
                    texture1C,
                ).unwrapU64()
            )
        }
    }

    override fun createMeshFromVox(vox: ByteArray): Mesh {
        ResourceScope.newConfinedScope().use { scope ->
            val voxC = copyToNativeArray(scope, vox)
            return RoastMesh(
                roast_backend_create_mesh_from_vox(
                    scope,
                    this.pointer,
                    voxC.address(),
                    voxC.byteSize()
                ).unwrapU64()
            )
        }
    }

    override fun createMeshWithGeometry(geometry: Mesh): Mesh {
        require(geometry is RoastMesh) { "RoastBackend only supports RoastMesh" }
        ResourceScope.newConfinedScope().use { scope ->
            return RoastMesh(
                roast_backend_create_mesh_with_geometry(
                    scope,
                    this.pointer,
                    geometry.pointer
                ).unwrapU64()
            )
        }
    }

    override fun getSize(): Vector2d {
        ResourceScope.newConfinedScope().use { scope ->
            val sizeC = roast_backend_get_size(
                scope,
                this.pointer
            ).unwrap(
                ForeignRoastResult_DVec2::`tag$get`,
                ForeignRoastResult_DVec2::`ok$slice`,
                ForeignRoastResult_DVec2::`err$slice`,
            )
            return Vector2d(
                DVec2.`x$get`(sizeC),
                DVec2.`y$get`(sizeC)
            )
        }
    }

    override fun showUI(withUI: UI.() -> Unit) {
        withUI(RoastUI(pointer))
    }

    override fun render(scene: Scene) {
        ResourceScope.newConfinedScope().use { scope ->
            val cameraC = ForeignCamera.allocate(scope).apply {
                ForeignCamera.`pos$slice`(this).apply {
                    DVec3.`x$set`(this, scene.camera.pos.x)
                    DVec3.`y$set`(this, scene.camera.pos.y)
                    DVec3.`z$set`(this, scene.camera.pos.z)
                }
                ForeignCamera.`yaw$set`(this, scene.camera.yaw)
                ForeignCamera.`pitch$set`(this, scene.camera.pitch)
                ForeignCamera.`fov$set`(this, scene.camera.fov)
            }
            // These always need to allocate at least 1 element, even
            // if we have no elements in the array.
            val sceneMeshIdsC = SegmentAllocator.ofScope(scope).allocateArray(
                int64_t,
                if (scene.sceneMeshes.size > 0) {
                    LongArray(scene.sceneMeshes.size) {
                        (scene.sceneMeshes[it] as RoastMesh).pointer
                    }
                } else {
                    LongArray(1)
                }
            )
            val guiMeshIdsC = SegmentAllocator.ofScope(scope).allocateArray(
                int64_t,
                if (scene.guiMeshes.size > 0) {
                    LongArray(scene.guiMeshes.size) {
                        (scene.guiMeshes[it] as RoastMesh).pointer
                    }
                } else {
                    LongArray(1)
                }
            )
            roast_backend_render(
                scope,
                this.pointer,
                cameraC,
                sceneMeshIdsC,
                scene.sceneMeshes.size.toLong(),
                guiMeshIdsC,
                scene.guiMeshes.size.toLong()
            ).unwrapNothing()
        }
    }

    external override fun exit()
}

/**
 * If the backend `panic!`s or if anything else goes wrong,
 * this exception will be thrown.
 */
class RoastException(msg: String) : RuntimeException(msg)
