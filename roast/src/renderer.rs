use std::sync::Arc;

use glam::{Mat4, Vec3, Vec4};
use lazy_static::lazy_static;
use vulkano::command_buffer::{
    AutoCommandBufferBuilder, CommandBufferUsage, DynamicState, PrimaryAutoCommandBuffer, SubpassContents,
};
use vulkano::device::{DeviceExtensions, Features};
use vulkano::format::ClearValue;

use crate::renderer::camera::Camera;
use crate::renderer::shader::{CameraBufferObjectData, PushConstantData, Vertex};
use crate::renderer::types::{UniformBuffer, VertexBuffer};
use crate::renderer::vulkan::{Frame, VulkanWrapper};

pub mod camera;
pub mod mesh;
pub mod shader;
pub mod types;
mod util;
pub mod vulkan;

pub const ENGINE_NAME: &str = "Roast/Umbryx";

#[cfg(all(debug_assertions))]
pub const ENABLE_DEBUG_UTILS: bool = false;
#[cfg(all(debug_assertions))]
pub const VALIDATION_LAYERS: &[&str] = &["VK_LAYER_LUNARG_standard_validation"];
#[cfg(not(debug_assertions))]
pub const ENABLE_DEBUG_UTILS: bool = false;
#[cfg(not(debug_assertions))]
pub const VALIDATION_LAYERS: &[&str] = &[];

/// Required device features for Roast.
pub fn get_device_features() -> Features {
    Features {
        sampler_anisotropy: true,
        shader_int8: true,
        shader_int16: true,
        shader_int64: true,
        ..Features::default()
    }
}

/// Required device extensions for Roast.
pub fn get_device_extensions() -> DeviceExtensions {
    DeviceExtensions {
        khr_swapchain: true,
        khr_8bit_storage: true,
        khr_16bit_storage: true,
        ..DeviceExtensions::none()
    }
}

lazy_static! {
    /// Clear value for the scene render pass.
    static ref SCENE_CLEAR: Vec<ClearValue> = vec![
        [0.0, 0.0, 0.0, 0.0].into(),
        ClearValue::Depth(1.0).into(),
    ];
}

/// Roast's backend renderer. Handles building
/// command buffers and dispatching them to Vulkan.
pub struct RoastRenderer {
    pub vulkan: VulkanWrapper,
    pub camera: Camera,
    triangle_of_doom: Arc<VertexBuffer>,
}

impl RoastRenderer {
    pub fn new(vulkan: VulkanWrapper) -> Self {
        let camera = Camera::default();
        let triangle_of_doom = vulkan.create_vertex_buffer(&[
            Vertex::new(Vec3::new(-1.0, -0.25, -0.5), Vec4::new(0.0, 1.0, 1.0, 1.0)).into(),
            Vertex::new(Vec3::new(-1.0, 0.25, 0.0), Vec4::new(0.0, 1.0, 0.5, 1.0)).into(),
            Vertex::new(Vec3::new(-1.0, -0.25, 0.5), Vec4::new(1.0, 0.5, 1.0, 1.0)).into(),
        ]);
        Self {
            vulkan,
            camera,
            triangle_of_doom,
        }
    }

    pub fn render(&mut self) {
        let frame = match self.vulkan.begin_frame() {
            None => return,
            Some(frame) => frame,
        };

        let mut builder = AutoCommandBufferBuilder::primary(
            self.vulkan.device.clone(),
            self.vulkan.queues.graphics.family(),
            CommandBufferUsage::OneTimeSubmit,
        )
        .unwrap();

        let camera_buffer = self.update_buffers();
        self.scene_pass(&frame, &mut builder, &camera_buffer);

        // For some reason IntelliJ thinks that the build() function on the next line is
        // PipelineLayoutDesc::build rather than AutoCommandBufferBuilder::build, so we
        // explicitly cast builder to an AutoCommandBufferBuilder to silence the error
        self.vulkan.submit_frame(
            frame,
            Arc::new(
                (builder as AutoCommandBufferBuilder<PrimaryAutoCommandBuffer<_>>)
                    .build()
                    .unwrap(),
            ),
        );
    }

    #[inline]
    fn update_buffers(&mut self) -> Arc<UniformBuffer<CameraBufferObjectData>> {
        Arc::new(
            self.vulkan
                .uniform_buffers
                .camera
                .next(self.camera.update_uniform_buffer(self.vulkan.swap_chain.dimensions()))
                .unwrap(),
        )
    }

    #[inline]
    fn scene_pass(
        &mut self,
        frame: &Frame,
        builder: &mut AutoCommandBufferBuilder<PrimaryAutoCommandBuffer>,
        camera_buffer: &Arc<UniformBuffer<CameraBufferObjectData>>,
    ) {
        builder
            .begin_render_pass(
                frame.framebuffers.scene.clone(),
                SubpassContents::Inline,
                (*SCENE_CLEAR).clone(),
            )
            .unwrap();

        let descriptor_set = Arc::new(
            self.vulkan
                .descriptor_sets
                .scene
                .next()
                .add_buffer(camera_buffer.clone())
                .unwrap()
                .build()
                .unwrap(),
        );

        let mut push = PushConstantData::default();
        push.model = Mat4::IDENTITY;

        builder
            .draw(
                self.vulkan.pipelines.scene.clone(),
                &DynamicState::none(),
                vec![self.triangle_of_doom.clone()],
                descriptor_set.clone(),
                push.clone(),
                std::iter::empty(),
            )
            .unwrap();

        builder.end_render_pass().unwrap();
    }
}
