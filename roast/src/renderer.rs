use std::collections::HashMap;
use std::sync::Arc;

use egui::CtxRef;
use image::DynamicImage;
use lazy_static::lazy_static;
use vulkano::command_buffer::{
    AutoCommandBufferBuilder, CommandBufferUsage, DynamicState, PrimaryAutoCommandBuffer, SubpassContents,
};
use vulkano::device::{DeviceExtensions, Features};
use vulkano::format::ClearValue;
use vulkano::sampler::Sampler;

use crate::renderer::camera::Camera;
use crate::renderer::mesh::Mesh;
use crate::renderer::scene::Scene;
use crate::renderer::shader::CameraBufferObjectData;
use crate::renderer::texture::{Texture, TextureSampling};
use crate::renderer::types::{GraphicsPipeline, UniformBuffer};
use crate::renderer::vulkan::{Frame, VulkanWrapper};

pub mod camera;
pub mod mesh;
pub mod scene;
pub mod shader;
pub mod texture;
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

/// Id to refer to a texture, so that
/// the Java side can locate a texture.
pub type TextureId = u64;

/// Id to refer to a mesh, so that
/// the Java side can locate a mesh.
pub type MeshId = u64;

/// Roast's backend renderer. Handles building
/// command buffers and dispatching them to Vulkan.
pub struct RoastRenderer {
    pub vulkan: VulkanWrapper,
    pub camera: Camera,
    pub gui: CtxRef,
    pub textures: HashMap<TextureId, Texture>,
    pub meshes: HashMap<MeshId, Mesh>,

    texture_id_counter: TextureId,
    mesh_id_counter: MeshId,
    default_texture: Texture,
}

impl RoastRenderer {
    pub fn new(vulkan: VulkanWrapper, default_texture: DynamicImage) -> Self {
        Self {
            default_texture: Texture::new(&vulkan, default_texture, TextureSampling::Pixel, true),
            vulkan,
            camera: Camera::default(),
            gui: CtxRef::default(),
            textures: HashMap::new(),
            meshes: HashMap::new(),
            texture_id_counter: 0,
            mesh_id_counter: 0,
        }
    }

    /// Registers a new texture with this renderer.
    pub fn register_texture(&mut self, texture: Texture) -> TextureId {
        let texture_id = self.texture_id_counter;
        self.textures.insert(texture_id, texture);
        self.texture_id_counter = self
            .texture_id_counter
            .checked_add(1)
            .expect("this is not okay (too many textures!)");
        texture_id
    }

    /// Registers a new mesh with this renderer.
    pub fn register_mesh(&mut self, mesh: Mesh) -> MeshId {
        let mesh_id = self.mesh_id_counter;
        self.meshes.insert(mesh_id, mesh);
        self.mesh_id_counter = self
            .mesh_id_counter
            .checked_add(1)
            .expect("this is not okay (too many meshes!)");
        mesh_id
    }

    fn get_texture_or_default(&self, id: Option<TextureId>) -> &Texture {
        id.map(|t| self.textures.get(&t).unwrap_or(&self.default_texture))
            .unwrap_or(&self.default_texture)
    }

    fn get_sampler_for_texture(&self, texture: &Texture) -> Arc<Sampler> {
        match texture.sampling() {
            TextureSampling::Smooth => self.vulkan.samplers.texture.clone(),
            TextureSampling::Pixel => self.vulkan.samplers.pixel_texture.clone(),
        }
    }

    /// The main render function. Renders a single frame of
    /// the provided scene and GUI.
    pub fn render(&mut self, scene: Scene) {
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

        self.scene_pass(&frame, &mut builder, &scene);

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

    /// Sorts and renders the meshes referenced by `mesh_ids`.
    fn render_meshes(
        &mut self,
        builder: &mut AutoCommandBufferBuilder<PrimaryAutoCommandBuffer>,
        pipeline: Arc<GraphicsPipeline>,
        camera_buffer: &Arc<UniformBuffer<CameraBufferObjectData>>,
        mesh_ids: &Vec<MeshId>,
    ) {
        let camera_descriptor_set = Arc::new(
            self.vulkan
                .descriptor_sets
                .camera
                .next()
                .add_buffer(camera_buffer.clone())
                .unwrap()
                .build()
                .unwrap(),
        );

        // TODO: bindless descriptor sets
        // Vulkano doesn't support these yet and support
        // is blocked on #1355 and #1599 at the very least.

        // To minimize descriptor sets and rebinds we build
        // a map between the resources needed for each mesh
        // and the meshes that use those resources.
        let mut descriptor_map = HashMap::new();
        for mesh_id in mesh_ids {
            let mesh = self.meshes.get(mesh_id).unwrap();
            if !descriptor_map.contains_key(&mesh.textures) {
                descriptor_map.insert(mesh.textures.clone(), Vec::new());
            }
            descriptor_map.get_mut(&mesh.textures).unwrap().push(mesh);
        }

        for descriptors in descriptor_map.keys() {
            let texture0 = self.get_texture_or_default(descriptors.0).clone();
            let texture1 = self.get_texture_or_default(descriptors.1).clone();
            let sampler0 = self.get_sampler_for_texture(&texture0);
            let sampler1 = self.get_sampler_for_texture(&texture1);

            let scene_descriptor_set = Arc::new(
                self.vulkan
                    .descriptor_sets
                    .scene
                    .next()
                    .add_sampled_image(texture0.image().clone(), sampler0)
                    .unwrap()
                    .add_sampled_image(texture1.image().clone(), sampler1)
                    .unwrap()
                    .build()
                    .unwrap(),
            );

            for mesh in descriptor_map.get(descriptors).unwrap() {
                builder
                    .draw_indexed(
                        pipeline.clone(),
                        &DynamicState::none(),
                        vec![mesh.vertex_buffer().clone()],
                        mesh.index_buffer().clone(),
                        (camera_descriptor_set.clone(), scene_descriptor_set.clone()),
                        mesh.fill_push_constants(),
                        std::iter::empty(),
                    )
                    .unwrap();
            }
        }
    }

    #[inline]
    fn scene_pass(
        &mut self,
        frame: &Frame,
        builder: &mut AutoCommandBufferBuilder<PrimaryAutoCommandBuffer>,
        scene: &Scene,
    ) {
        let perspective_camera_buffer = Arc::new(
            self.vulkan
                .uniform_buffers
                .camera
                .next(
                    self.camera
                        .update_uniform_buffer(self.vulkan.swap_chain.dimensions(), false),
                )
                .unwrap(),
        );

        let ortho_camera_buffer = Arc::new(
            self.vulkan
                .uniform_buffers
                .camera
                .next(
                    self.camera
                        .update_uniform_buffer(self.vulkan.swap_chain.dimensions(), true),
                )
                .unwrap(),
        );

        builder
            .begin_render_pass(
                frame.framebuffers.scene.clone(),
                SubpassContents::Inline,
                (*SCENE_CLEAR).clone(),
            )
            .unwrap();
        self.render_meshes(
            builder,
            self.vulkan.pipelines.scene.clone(),
            &perspective_camera_buffer,
            &scene.scene_meshes,
        );
        builder.next_subpass(SubpassContents::Inline).unwrap();
        self.render_meshes(
            builder,
            self.vulkan.pipelines.gui.clone(),
            &ortho_camera_buffer,
            &scene.gui_meshes,
        );
        builder.end_render_pass().unwrap();
    }
}
