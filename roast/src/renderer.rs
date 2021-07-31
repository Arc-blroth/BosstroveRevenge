use std::borrow::Cow;
use std::collections::{BTreeMap, HashMap};
use std::sync::Arc;

use egui::paint::ClippedShape;
use egui::{FontDefinitions, FontFamily, Style, TextStyle};
use egui_winit_platform::{Platform, PlatformDescriptor};
use image::DynamicImage;
use lazy_static::lazy_static;
use vulkano::command_buffer::{
    AutoCommandBufferBuilder, CommandBufferUsage, PrimaryAutoCommandBuffer, SubpassContents,
};
use vulkano::device::{DeviceExtensions, Features};
use vulkano::format::ClearValue;
use vulkano::sampler::Sampler;

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
        khr_storage_buffer_storage_class: true,
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
    pub gui: Platform,
    pub textures: HashMap<TextureId, Texture>,
    pub meshes: HashMap<MeshId, Mesh>,

    texture_id_counter: TextureId,
    mesh_id_counter: MeshId,
    default_texture: Texture,
}

impl RoastRenderer {
    pub fn new(vulkan: VulkanWrapper, default_texture: DynamicImage) -> Self {
        let size = vulkan.surface.window().inner_size();
        let scale_factor = vulkan.surface.window().scale_factor();

        Self {
            default_texture: Texture::new(&vulkan, default_texture, TextureSampling::Pixel, true),
            vulkan,
            gui: Platform::new(PlatformDescriptor {
                physical_width: size.width as u32,
                physical_height: size.height as u32,
                scale_factor,
                font_definitions: FontDefinitions::default(),
                style: Style::default(),
            }),
            textures: HashMap::new(),
            meshes: HashMap::new(),
            texture_id_counter: 0,
            mesh_id_counter: 0,
        }
    }

    pub fn init(&self) {
        // Init fonts
        type FontData = Cow<'static, [u8]>;

        let mut font_data: BTreeMap<String, FontData> = BTreeMap::new();
        let mut fonts_for_family = BTreeMap::new();
        let mut family_and_size = BTreeMap::new();

        font_data.insert(
            "Cascadia Mono".to_owned(),
            Cow::Borrowed(include_bytes!(
                "../target/fonts/cascadia/ttf/static/CascadiaMono-Regular.ttf"
            )),
        );

        fonts_for_family.insert(FontFamily::Proportional, vec!["Cascadia Mono".to_owned()]);
        fonts_for_family.insert(FontFamily::Monospace, vec!["Cascadia Mono".to_owned()]);

        family_and_size.insert(TextStyle::Small, (FontFamily::Proportional, 8.0));
        family_and_size.insert(TextStyle::Body, (FontFamily::Proportional, 16.0));
        family_and_size.insert(TextStyle::Button, (FontFamily::Proportional, 16.0));
        family_and_size.insert(TextStyle::Heading, (FontFamily::Proportional, 32.0));
        family_and_size.insert(TextStyle::Monospace, (FontFamily::Monospace, 16.0));

        self.gui.context().set_fonts(FontDefinitions {
            font_data,
            fonts_for_family,
            family_and_size,
        });

        // Show window
        self.vulkan.surface.window().set_visible(true);
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
    pub fn render(&mut self, scene: Scene, gui_data: Vec<ClippedShape>) {
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

        self.scene_pass(&frame, &mut builder, &scene, gui_data);

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
        mesh_ids: &[MeshId],
    ) {
        // If there is nothing to render then return early
        if mesh_ids.len() == 0 {
            return;
        }

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

        // To minimize descriptor sets and rebinds we keep
        // track of what descriptor is currently rebound and
        // rebind as little as possible.
        let mut current_descriptor: Option<(Option<TextureId>, Option<TextureId>)> = None;
        let mut scene_descriptor_set = None;

        for mesh_id in mesh_ids {
            let mesh = self.meshes.get(mesh_id).unwrap();

            // Do we need to rebind?
            let should_rebind = match current_descriptor {
                None => true,
                Some(desc) => 'rebind_checker: {
                    if let Some(texture0) = mesh.textures.0 {
                        if desc.0 != Some(texture0) {
                            break 'rebind_checker true;
                        }
                    }
                    if let Some(texture1) = mesh.textures.1 {
                        if desc.1 != Some(texture1) {
                            break 'rebind_checker true;
                        }
                    }
                    false
                }
            };

            if should_rebind {
                let texture0 = self.get_texture_or_default(mesh.textures.0).clone();
                let texture1 = self.get_texture_or_default(mesh.textures.1).clone();
                let sampler0 = self.get_sampler_for_texture(&texture0);
                let sampler1 = self.get_sampler_for_texture(&texture1);

                scene_descriptor_set = Some(Arc::new(
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
                ));

                current_descriptor = Some(mesh.textures);
            }

            builder
                .draw_indexed(
                    pipeline.clone(),
                    &self.vulkan.dynamic_state,
                    vec![mesh.vertex_buffer()],
                    mesh.index_buffer(),
                    (camera_descriptor_set.clone(), scene_descriptor_set.clone().unwrap()),
                    mesh.fill_push_constants(),
                    std::iter::empty(),
                )
                .unwrap();
        }
    }

    #[inline]
    fn scene_pass(
        &mut self,
        frame: &Frame,
        builder: &mut AutoCommandBufferBuilder<PrimaryAutoCommandBuffer>,
        scene: &Scene,
        gui_data: Vec<ClippedShape>,
    ) {
        let swap_chain_dimensions = self.vulkan.swap_chain.dimensions().map(|x| x as f32);

        let perspective_camera_buffer = Arc::new(
            self.vulkan
                .uniform_buffers
                .camera
                .next(scene.camera.update_uniform_buffer(swap_chain_dimensions, false))
                .unwrap(),
        );

        let ortho_camera_buffer = Arc::new(
            self.vulkan
                .uniform_buffers
                .camera
                .next(scene.camera.update_uniform_buffer(swap_chain_dimensions, true))
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

        // The GUI Painter will call builder.next_subpass
        self.vulkan
            .gui_painter
            .draw(
                builder,
                &self.vulkan.dynamic_state,
                swap_chain_dimensions,
                &self.gui.context(),
                gui_data,
            )
            .unwrap();
        self.render_meshes(
            builder,
            self.vulkan.pipelines.gui.clone(),
            &ortho_camera_buffer,
            &scene.gui_meshes,
        );

        builder.end_render_pass().unwrap();
    }
}
