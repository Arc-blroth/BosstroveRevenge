//! Helper types that group together related
//! things inside VulkanWrapper

use std::sync::Arc;

use vulkano::buffer::cpu_pool::CpuBufferPoolSubbuffer;
use vulkano::buffer::{BufferAccess, CpuBufferPool, TypedBufferAccess};
use vulkano::descriptor::descriptor_set::FixedSizeDescriptorSetsPool;
use vulkano::device::Queue;
use vulkano::image::view::ImageView;
use vulkano::image::AttachmentImage;
use vulkano::memory::pool::StdMemoryPool;
use vulkano::pipeline::GraphicsPipelineAbstract;
use vulkano::render_pass::{FramebufferAbstract, RenderPass};
use vulkano::sampler::Sampler;
use vulkano::swapchain::Surface;
use winit::window::Window;

use crate::renderer::shader::CameraBufferObjectData;

pub type Index = u32;
pub type WindowSurface = Surface<Window>;
pub type GraphicsPipeline = dyn GraphicsPipelineAbstract + Send + Sync;
pub type Framebuffer = dyn FramebufferAbstract + Send + Sync;
pub type VertexBuffer = dyn BufferAccess + Send + Sync;
pub type IndexBuffer = dyn TypedBufferAccess<Content = [Index]> + Send + Sync;
pub type UniformBuffer<T> = CpuBufferPoolSubbuffer<T, Arc<StdMemoryPool>>;
pub type ImageWithView<T> = ImageView<Arc<T>>;

pub struct Queues {
    pub graphics: Arc<Queue>,
    pub transfer: Arc<Queue>,
    pub present: Arc<Queue>,
}

#[derive(Default)]
pub struct QueueFamilyIndices {
    pub(super) graphics: Option<i32>,
    pub(super) present: Option<i32>,
    pub(super) transfer: Option<i32>,
}

impl QueueFamilyIndices {
    pub(super) fn is_complete(&self) -> bool {
        self.graphics.is_some() && self.present.is_some() && self.transfer.is_some()
    }
}

#[derive(Clone)]
pub struct RenderPasses {
    // pub shadow: Arc<RenderPass>,
    pub scene: Arc<RenderPass>,
}

#[derive(Clone)]
pub struct Pipelines {
    // pub shadow: Arc<GraphicsPipeline>,
    pub scene: Arc<GraphicsPipeline>,
    pub gui: Arc<GraphicsPipeline>,
}

#[derive(Clone)]
pub struct Framebuffers {
    // pub shadow: Arc<Framebuffer>,
    pub scene: Arc<Framebuffer>,
}

#[derive(Clone)]
pub struct FramebufferAttachments {
    // pub shadow: Arc<ImageWithView<AttachmentImage>>,
    pub depth: Arc<ImageWithView<AttachmentImage>>,
}

pub struct Samplers {
    pub texture: Arc<Sampler>,
    pub pixel_texture: Arc<Sampler>,
    pub shadow: Arc<Sampler>,
}

pub struct UniformBuffers {
    // pub shadow: CpuBufferPool<ShadowBufferObjectData>,
    pub camera: CpuBufferPool<CameraBufferObjectData>,
    // pub lights: CpuBufferPool<LightBufferObjectData>,
    pub dynamic_buffer_alignment: usize,
}

#[derive(Clone)]
pub struct DescriptorSetPools {
    // pub shadow: FixedSizeDescriptorSetsPool,
    pub camera: FixedSizeDescriptorSetsPool,
    pub scene: FixedSizeDescriptorSetsPool,
}
