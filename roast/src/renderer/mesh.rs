use std::sync::Arc;

use vulkano::buffer::{BufferUsage, ImmutableBuffer};
use vulkano::sync::GpuFuture;

use crate::renderer::shader::GpuVertex;
use crate::renderer::types::{Index, IndexBuffer, VertexBuffer};
use crate::renderer::vulkan::VulkanWrapper;

impl VulkanWrapper {
    /// Creates a vertex buffer using the graphics queue on `vulkan`.
    pub fn create_vertex_buffer(&self, vertices: &[GpuVertex]) -> Arc<VertexBuffer> {
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
