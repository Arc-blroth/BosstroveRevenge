use std::sync::Arc;

use glam::{Mat4, Vec2, Vec4};
use vulkano::buffer::{BufferUsage, ImmutableBuffer};
use vulkano::sync::GpuFuture;

use crate::renderer::shader::{PushConstantData, Vertex, VertexType};
use crate::renderer::types::{Index, IndexBuffer, VertexBuffer};
use crate::renderer::vulkan::VulkanWrapper;
use crate::renderer::TextureId;

impl VulkanWrapper {
    /// Creates a vertex buffer using the graphics queue on `vulkan`.
    pub fn create_vertex_buffer<T>(&self, vertices: &[T]) -> Arc<VertexBuffer>
    where
        T: 'static + Send + Sync + Sized + Clone,
    {
        let (buffer, future) = ImmutableBuffer::from_iter(
            vertices.iter().cloned(),
            BufferUsage::vertex_buffer(),
            self.queues.graphics.clone(),
        )
        .unwrap();
        future.flush().unwrap();
        buffer
    }

    /// Creates an index buffer using the graphics queue on `vulkan`.
    pub fn create_index_buffer(&self, indices: &[Index]) -> Arc<IndexBuffer> {
        let (buffer, future) = ImmutableBuffer::from_iter(
            indices.iter().cloned(),
            BufferUsage::index_buffer(),
            self.queues.graphics.clone(),
        )
        .unwrap();
        future.flush().unwrap();
        buffer
    }
}

/// A `Mesh` consists of the geometry and associated textures
/// needed to render a mesh, as well as its model matrix,
/// texture offsets, an optional overlay color, and its opacity.
#[derive(Clone)]
pub struct Mesh {
    vertex_buffer: Arc<VertexBuffer>,
    index_buffer: Arc<IndexBuffer>,
    vertex_type: VertexType,
    pub textures: (Option<TextureId>, Option<TextureId>),

    pub transform: Mat4,
    pub texture_offsets: (Vec2, Vec2),
    pub overlay_color: Option<Vec4>,
    pub opacity: f32,
}

impl Mesh {
    /// Builds a mesh from the given vertices,
    /// indices, and textures for the given `vulkan` instance.
    pub fn build(
        vertices: &[Vertex],
        indices: &[Index],
        vertex_type: VertexType,
        texture0: Option<TextureId>,
        texture1: Option<TextureId>,
        vulkan: &VulkanWrapper,
    ) -> Self {
        Self {
            vertex_buffer: vulkan.create_vertex_buffer(vertices),
            index_buffer: vulkan.create_index_buffer(indices),
            vertex_type,
            textures: (texture0, texture1),
            transform: Mat4::IDENTITY,
            texture_offsets: (Vec2::ZERO, Vec2::ZERO),
            overlay_color: None,
            opacity: 1.0,
        }
    }

    /// Builds a mesh with the same geometry as an existing Mesh.
    pub fn with_geometry(geometry: &Mesh) -> Self {
        Self {
            vertex_buffer: geometry.vertex_buffer.clone(),
            index_buffer: geometry.index_buffer.clone(),
            vertex_type: geometry.vertex_type,
            textures: geometry.textures,
            transform: Mat4::IDENTITY,
            texture_offsets: (Vec2::ZERO, Vec2::ZERO),
            overlay_color: None,
            opacity: 1.0,
        }
    }

    pub fn vertex_buffer(&self) -> &Arc<VertexBuffer> {
        &self.vertex_buffer
    }

    pub fn index_buffer(&self) -> &Arc<IndexBuffer> {
        &self.index_buffer
    }

    pub fn vertex_type(&self) -> &VertexType {
        &self.vertex_type
    }

    /// Fills and returns the PushConstantData that should be passed
    /// to the command builder when drawing this mesh.
    pub fn fill_push_constants(&self) -> PushConstantData {
        PushConstantData {
            model: self.transform,
            tex_offsets: Vec4::new(
                self.texture_offsets.0[0],
                self.texture_offsets.0[1],
                self.texture_offsets.1[0],
                self.texture_offsets.1[1],
            ),
            overlay_color: self.overlay_color.unwrap_or(Vec4::ONE),
            opacity: self.opacity,
            vertex_type: self.vertex_type,
        }
    }
}
