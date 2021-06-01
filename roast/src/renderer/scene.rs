use crate::renderer::MeshId;

/// See `ai.arcblroth.boss.render.Scene`
/// A Scene stores all the data passed from the
/// high-level code needed to render a single
/// frame of Bosstrove's Revenge.
pub struct Scene {
    pub scene_meshes: Vec<MeshId>,
    pub gui_meshes: Vec<MeshId>,
}
