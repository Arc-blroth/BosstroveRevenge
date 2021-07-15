package ai.arcblroth.boss.render

/**
 * A `Scene` stores all the information needed to render
 * a frame of Bosstrove's Revenge.
 */
class Scene(
    /**
     * The camera to use for rendering the [sceneMeshes].
     */
    val camera: Camera = Camera(),

    /**
     * The main array of meshes to render. The backend
     * will render these in a three-dimensional context
     * with depth and lighting.
     */
    val sceneMeshes: ArrayList<Mesh> = arrayListOf(),

    /**
     * An array of meshes to render on top of the GUI.
     * The backend will render these in a two-dimensional
     * context, with later meshes rendering on top of
     * earlier ones.
     */
    val guiMeshes: ArrayList<Mesh> = arrayListOf(),
)
