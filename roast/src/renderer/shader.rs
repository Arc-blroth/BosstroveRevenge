use glam::{Mat4, Vec3, Vec4};

vulkano_shaders::shader! {
    ty: "vertex",
    bytes: "target/shader/shader.spv",
}

#[repr(C)]
#[derive(Default, Copy, Clone, PartialEq)]
pub struct Vertex {
    pub pos: Vec3,
    pub color: Vec4,
}

impl Vertex {
    pub fn new(pos: Vec3, color: Vec4) -> Self {
        Vertex { pos, color }
    }
}

/// `GpuVertex` has the same data layout as `Vertex`
/// but uses arrays rather than glam structs
/// so that `impl_vertex!` works on it.
#[repr(C)]
#[derive(Default, Copy, Clone, PartialEq)]
pub struct GpuVertex {
    pub pos: [f32; 3],
    pub color: [f32; 4],
}

vulkano::impl_vertex!(GpuVertex, pos, color);

impl From<Vertex> for GpuVertex {
    fn from(vertex: Vertex) -> Self {
        Self {
            pos: vertex.pos.to_array(),
            color: vertex.color.to_array(),
        }
    }
}

#[repr(C)]
#[derive(Copy, Clone, PartialEq)]
pub struct CameraBufferObjectData {
    pub view: Mat4,
    pub proj: Mat4,
}

#[repr(C)]
#[derive(Default, Copy, Clone, PartialEq)]
pub struct PushConstantData {
    pub model: Mat4,
}
