use glam::{Mat4, Vec4};

vulkano_shaders::shader! {
    ty: "vertex",
    bytes: "target/shader/shader.spv",
}

/// The vertex primitive used as an input to the
/// shader. This can specify a position and one of
/// - 1 color (4 floats)
/// - 2 texture coordinates (4 floats)
#[repr(C)]
#[derive(Default, Copy, Clone, PartialEq)]
pub struct Vertex {
    /// position
    pub pos: [f32; 3],
    /// color or texture coordinates
    pub color_tex: [f32; 4],
}

/// How to interpret the vertex data used in a mesh.
#[repr(u32)]
#[derive(Copy, Clone, PartialEq, Eq)]
pub enum VertexType {
    /// `color_tex` holds a single color.
    COLOR,
    /// `color_tex` holds one texture coordinate.
    TEX1,
    /// `color_tex` holds two texture coordinates.
    TEX2,
}

impl Default for VertexType {
    fn default() -> Self {
        VertexType::COLOR
    }
}

impl Vertex {
    /// Builds a new Vertex with a position and color.
    pub fn pos_color(pos: [f32; 3], color: [f32; 4]) -> Self {
        Self { pos, color_tex: color }
    }

    /// Builds a new Vertex with a position and texture coordinate.
    pub fn pos_tex(pos: [f32; 3], tex: [f32; 2]) -> Self {
        Self {
            pos,
            color_tex: [tex[0], tex[1], 0.0, 0.0],
        }
    }

    /// Builds a new Vertex with a position and two texture coordinates.
    pub fn pos_tex2(pos: [f32; 3], tex1: [f32; 2], tex2: [f32; 2]) -> Self {
        Self {
            pos,
            color_tex: [tex1[0], tex1[1], tex2[0], tex2[1]],
        }
    }
}

vulkano::impl_vertex!(Vertex, pos, color_tex);

#[repr(C)]
#[derive(Copy, Clone, PartialEq)]
pub struct CameraBufferObjectData {
    pub view: Mat4,
    pub proj: Mat4,
}

/// The push constants used as an input to
/// the shader. This contains the model
/// matrix for the mesh drawn with these
/// push constants, what type of vertices
/// the mesh uses, and additional colors
/// to overlay on the mesh.
///
/// Per the Vulkan spec, this **must not**
/// exceed 128 bytes.
#[repr(C)]
#[derive(Default, Copy, Clone, PartialEq)]
pub struct PushConstantData {
    pub model: Mat4,             // 64 bytes
    pub tex_offsets: Vec4,       // 16 bytes
    pub overlay_color: Vec4,     // 16 bytes
    pub opacity: f32,            // 4 bytes
    pub vertex_type: VertexType, // 1 byte (padded to 2),
}
