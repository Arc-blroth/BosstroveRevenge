use std::collections::HashSet;
use std::iter::FromIterator;
use std::sync::Arc;

use egui_vulkano::Painter;
use vulkano::buffer::CpuBufferPool;
use vulkano::command_buffer::PrimaryAutoCommandBuffer;
use vulkano::descriptor::descriptor_set::FixedSizeDescriptorSetsPool;
use vulkano::device::Device;
use vulkano::format::Format;
use vulkano::image::attachment::AttachmentImage;
use vulkano::image::view::ImageView;
use vulkano::image::{ImageAccess, ImageUsage, SwapchainImage};
use vulkano::instance::debug::{DebugCallback, MessageSeverity, MessageType};
use vulkano::instance::{ApplicationInfo, Instance, PhysicalDevice, Version};
use vulkano::pipeline::depth_stencil::{Compare, DepthBounds, DepthStencil};
use vulkano::pipeline::viewport::Viewport;
use vulkano::pipeline::GraphicsPipeline as VulkanoGraphicsPipeline;
use vulkano::render_pass::{Framebuffer as VulkanoFramebuffer, Subpass};
use vulkano::sampler::{BorderColor, Filter, MipmapMode, Sampler, SamplerAddressMode};
use vulkano::swapchain::{
    acquire_next_image, AcquireError, ColorSpace, CompositeAlpha, FullscreenExclusive, PresentMode, Surface, Swapchain,
    SwapchainAcquireFuture,
};
use vulkano::sync::{GpuFuture, SharingMode};
use vulkano_win::VkSurfaceBuild;
use winit::dpi::{PhysicalPosition, PhysicalSize};
use winit::event_loop::EventLoop;
use winit::window::{Fullscreen, Window, WindowBuilder};

use crate::backend::{FullscreenMode, RendererSettings};
use crate::renderer::shader::{Shader, Vertex};
use crate::renderer::types::*;
use crate::renderer::util::*;
use crate::renderer::{get_device_extensions, get_device_features, ENABLE_DEBUG_UTILS, ENGINE_NAME, VALIDATION_LAYERS};

/// Roast's Vulkan wrapper. Handles the complicated
/// process of setting up Vulkan, but doesn't actually
/// render anything by itself.
///
/// _Totally not copied from Umbryx or anything_
pub struct VulkanWrapper {
    pub(super) instance: Arc<Instance>,
    pub surface: Arc<WindowSurface>,
    #[allow(unused)]
    debug_callback: Option<DebugCallback>,

    physical_device_index: usize,
    pub(super) device: Arc<Device>,

    pub(super) queues: Queues,

    pub(super) swap_chain: Arc<Swapchain<Window>>,
    pub(super) swap_chain_images: Vec<Arc<ImageWithView<SwapchainImage<Window>>>>,

    pub(super) shader: Shader,
    pub(super) render_passes: RenderPasses,
    pub(super) pipelines: Pipelines,
    pub(super) gui_painter: Painter,

    pub(super) swap_chain_framebuffers: Vec<Framebuffers>,
    pub(super) framebuffer_attachments: FramebufferAttachments,

    pub(super) samplers: Samplers,
    pub(super) uniform_buffers: UniformBuffers,
    pub(super) descriptor_sets: DescriptorSetPools,

    previous_frame_end: Option<Box<dyn GpuFuture>>,
    should_recreate_swap_chain: bool,
}

impl VulkanWrapper {
    pub fn new(event_loop: &EventLoop<()>, app_name: String, settings: RendererSettings) -> Self {
        let instance = Self::init_instance(app_name.clone());
        let surface = Self::init_surface(event_loop, instance.clone(), app_name, settings);
        let debug_callback = Self::init_debug_callback(&instance);
        let physical_device_index = Self::init_physical_device(&instance, &surface);
        let (device, queues) = Self::init_logical_device(&instance, &surface, physical_device_index);
        let (swap_chain, swap_chain_images) =
            Self::create_swap_chain(&instance, &surface, physical_device_index, &device, &queues, None);
        let framebuffer_attachments = Self::create_framebuffer_attachments(&device, &surface, &swap_chain);
        let shader = Shader::load(device.clone()).expect("Could not load shader!");
        let render_passes = Self::create_render_passes(&device, swap_chain.format(), &framebuffer_attachments);
        let pipelines = Self::create_pipelines(&device, swap_chain.dimensions(), &render_passes, &shader);
        let gui_painter = Self::create_gui_painter(&device, &queues, &render_passes);
        let swap_chain_framebuffers =
            Self::create_framebuffers(&swap_chain_images, &render_passes, &framebuffer_attachments);
        let samplers = Self::create_texture_samplers(&device);
        let previous_frame_end = Some(Box::new(vulkano::sync::now(device.clone())) as Box<_>);
        let uniform_buffers = Self::create_uniform_buffers(&device);
        let descriptor_sets = Self::create_descriptor_sets_pool(pipelines.clone());

        Self {
            instance,
            surface,
            debug_callback,
            physical_device_index,
            device,
            queues,
            swap_chain,
            swap_chain_images,
            shader,
            render_passes,
            pipelines,
            gui_painter,
            swap_chain_framebuffers,
            framebuffer_attachments,
            samplers,
            uniform_buffers,
            descriptor_sets,
            previous_frame_end,
            should_recreate_swap_chain: false,
        }
    }

    fn recreate_swap_chain(&mut self) {
        let (swap_chain, images) = Self::create_swap_chain(
            &self.instance,
            &self.surface,
            self.physical_device_index,
            &self.device,
            &self.queues,
            Some(self.swap_chain.clone()),
        );
        self.swap_chain = swap_chain;
        self.swap_chain_images = images;
        self.framebuffer_attachments =
            Self::create_framebuffer_attachments(&self.device, &self.surface, &self.swap_chain);
        self.render_passes =
            Self::create_render_passes(&self.device, self.swap_chain.format(), &self.framebuffer_attachments);
        self.pipelines = Self::create_pipelines(
            &self.device,
            self.swap_chain.dimensions(),
            &self.render_passes,
            &self.shader,
        );
        self.gui_painter = Self::create_gui_painter(&self.device, &self.queues, &self.render_passes);
        self.swap_chain_framebuffers = Self::create_framebuffers(
            &self.swap_chain_images,
            &self.render_passes,
            &self.framebuffer_attachments,
        );
        self.descriptor_sets = Self::create_descriptor_sets_pool(self.pipelines.clone());
        self.uniform_buffers = Self::create_uniform_buffers(&self.device);
    }

    #[inline]
    fn init_instance(app_name: String) -> Arc<Instance> {
        if ENABLE_DEBUG_UTILS && !check_validation_layer_support() {
            panic!("Missing required validation layers!");
        }

        let app_info = ApplicationInfo {
            application_name: Some(app_name.into()),
            application_version: Some(Version {
                major: 0,
                minor: 1,
                patch: 0,
            }),
            engine_name: Some(ENGINE_NAME.into()),
            engine_version: Some(Version {
                major: 0,
                minor: 1,
                patch: 0,
            }),
        };

        let required_extensions = get_required_extensions();

        if ENABLE_DEBUG_UTILS && check_validation_layer_support() {
            Instance::new(Some(&app_info), &required_extensions, VALIDATION_LAYERS.iter().cloned())
                .expect("Failed to create Vulkan instance")
        } else {
            Instance::new(Some(&app_info), &required_extensions, None).expect("Failed to create Vulkan instance")
        }
    }

    #[inline]
    fn init_surface(
        event_loop: &EventLoop<()>,
        instance: Arc<Instance>,
        app_name: String,
        settings: RendererSettings,
    ) -> Arc<WindowSurface> {
        let monitor = event_loop.primary_monitor().unwrap();
        let video_mode = monitor.video_modes().min().unwrap();
        let renderer_size = settings.renderer_size;
        let window_size = PhysicalSize::new(
            (video_mode.size().width as f64 * renderer_size.0).round() as u32,
            (video_mode.size().height as f64 * renderer_size.1).round() as u32,
        );
        let window_pos = PhysicalPosition::new(
            (video_mode.size().width as f64 * ((1.0 - renderer_size.0) * 0.5)).round() as u32,
            (video_mode.size().height as f64 * ((1.0 - renderer_size.1) * 0.5)).round() as u32,
        );
        let surface = WindowBuilder::new()
            .with_title(app_name)
            .with_visible(false)
            .with_resizable(settings.fullscreen_mode == FullscreenMode::None)
            .with_decorations(settings.fullscreen_mode == FullscreenMode::None)
            .with_transparent(settings.transparent)
            .with_inner_size(window_size)
            .with_fullscreen(match settings.fullscreen_mode {
                FullscreenMode::Borderless => Some(Fullscreen::Borderless(Some(monitor))),
                FullscreenMode::Exclusive => Some(Fullscreen::Exclusive(video_mode)),
                _ => None,
            })
            .build_vk_surface(&event_loop, instance)
            .unwrap();
        surface.window().set_outer_position(window_pos);
        surface
    }

    #[inline]
    fn init_debug_callback(instance: &Arc<Instance>) -> Option<DebugCallback> {
        if !ENABLE_DEBUG_UTILS {
            None
        } else {
            Some(
                DebugCallback::new(
                    &instance,
                    MessageSeverity::errors_and_warnings(),
                    MessageType::all(),
                    |msg| log::warn!("{}", msg.description),
                )
                .expect("Failed to set up debug callback"),
            )
        }
    }

    #[inline]
    fn init_physical_device(instance: &Arc<Instance>, surface: &Arc<WindowSurface>) -> usize {
        PhysicalDevice::enumerate(&instance)
            .position(|device| is_physical_device_suitable(&device, &surface))
            .expect("Could not find a suitable GPU!")
    }

    #[inline]
    fn init_logical_device(
        instance: &Arc<Instance>,
        surface: &Arc<WindowSurface>,
        physical_device_index: usize,
    ) -> (Arc<Device>, Queues) {
        let physical_device = PhysicalDevice::from_index(&instance, physical_device_index).unwrap();
        let indices = find_queue_families(&physical_device, &surface);

        let families = [
            indices.graphics.unwrap(),
            indices.present.unwrap(),
            indices.transfer.unwrap(),
        ];
        let unique_queue_families: HashSet<&i32> = HashSet::from_iter(families.iter());

        let queue_priority = 1.0;
        let queue_families = unique_queue_families.iter().map(|i| {
            (
                physical_device.queue_families().nth(**i as usize).unwrap(),
                queue_priority,
            )
        });

        let (device, mut queues) = Device::new(
            physical_device,
            &get_device_features(),
            &get_device_extensions(),
            queue_families,
        )
        .expect("Failed to create logical device!");

        let graphics_queue = queues.next().unwrap();
        let present_queue = queues.next().unwrap_or_else(|| graphics_queue.clone());
        let transfer_queue = queues.next().unwrap_or_else(|| graphics_queue.clone());

        (
            device,
            Queues {
                graphics: graphics_queue,
                present: present_queue,
                transfer: transfer_queue,
            },
        )
    }

    fn create_swap_chain(
        instance: &Arc<Instance>,
        surface: &Arc<Surface<Window>>,
        physical_device_index: usize,
        device: &Arc<Device>,
        queues: &Queues,
        old_swapchain: Option<Arc<Swapchain<Window>>>,
    ) -> (Arc<Swapchain<Window>>, Vec<Arc<ImageWithView<SwapchainImage<Window>>>>) {
        let physical_device = PhysicalDevice::from_index(&instance, physical_device_index).unwrap();
        let capabilities = surface
            .capabilities(physical_device)
            .expect("Failed to get window capabilities");
        let extent = if let Some(current_extent) = capabilities.current_extent {
            current_extent
        } else {
            let window_size = surface.window().inner_size().clone();
            let mut actual_extent = [window_size.width as u32, window_size.height as u32];
            actual_extent[0] =
                capabilities.min_image_extent[0].max(capabilities.max_image_extent[0].min(actual_extent[0]));
            actual_extent[1] =
                capabilities.min_image_extent[1].max(capabilities.max_image_extent[1].min(actual_extent[1]));
            actual_extent
        };

        let (swap_chain, images) = if old_swapchain.is_none() {
            let surface_format = *capabilities
                .supported_formats
                .iter()
                .find(|(format, color_space)| {
                    *format == Format::B8G8R8A8Unorm && *color_space == ColorSpace::ExtendedSrgbLinear
                })
                .unwrap_or_else(|| &capabilities.supported_formats[0]);

            let present_mode = if capabilities.present_modes.mailbox {
                PresentMode::Mailbox
            } else if capabilities.present_modes.immediate {
                PresentMode::Immediate
            } else {
                PresentMode::Fifo
            };

            let mut image_count = capabilities.min_image_count + 1;
            if capabilities.max_image_count.is_some() && image_count > capabilities.max_image_count.unwrap() {
                image_count = capabilities.max_image_count.unwrap();
            }

            let image_usage = ImageUsage {
                color_attachment: true,
                ..ImageUsage::none()
            };

            let indices = find_queue_families(&physical_device, &surface);

            let sharing: SharingMode = if indices.graphics != indices.present {
                vec![&queues.graphics, &queues.present].as_slice().into()
            } else {
                (&queues.graphics).into()
            };

            Swapchain::start(device.clone(), surface.clone())
                .num_images(image_count)
                .format(surface_format.0)
                .dimensions(extent)
                .usage(image_usage)
                .sharing_mode(sharing)
                .transform(capabilities.current_transform)
                .composite_alpha(CompositeAlpha::Opaque)
                .present_mode(present_mode)
                .fullscreen_exclusive(FullscreenExclusive::AppControlled)
                .clipped(false)
                .color_space(ColorSpace::SrgbNonLinear)
                .build()
                .expect("Failed to create swap chain!")
        } else {
            Swapchain::recreate(&old_swapchain.unwrap())
                .dimensions(extent)
                .build()
                .expect("Failed to recreate swap chain!")
        };

        let images = images
            .into_iter()
            .map(|image| ImageView::new(image.clone()).unwrap())
            .collect::<Vec<_>>();

        (swap_chain, images)
    }

    fn create_framebuffer_attachments(
        device: &Arc<Device>,
        surface: &Arc<WindowSurface>,
        _swap_chain: &Arc<Swapchain<Window>>,
    ) -> FramebufferAttachments {
        let physical_window_size = surface.window().inner_size().clone();
        let window_size = [physical_window_size.width as u32, physical_window_size.height as u32];

        macro_rules! new_attachment {
            ($ctor:ident($($arg:expr),*$(,)?)) => {
                ImageView::new(
                    AttachmentImage::$ctor(
                        $($arg),*
                    )
                    .unwrap()
                )
                .unwrap()
            }
        }

        FramebufferAttachments {
            // shadow: new_attachment!(sampled(device.clone(), [1024, 1024], Format::D32Sfloat)),
            depth: new_attachment!(input_attachment(device.clone(), window_size, Format::D32Sfloat)),
        }
    }

    fn create_render_passes(
        device: &Arc<Device>,
        color_format: Format,
        framebuffer_attachments: &FramebufferAttachments,
    ) -> RenderPasses {
        // let shadow = Arc::new(
        //     vulkano::single_pass_renderpass!(
        //         device.clone(),
        //         attachments: {
        //             depth_stencil: {
        //                 load: Clear,
        //                 store: Store,
        //                 format: framebuffer_attachments.shadow.image().format(),
        //                 samples: 1,
        //             }
        //         },
        //         pass: {
        //             color: [],
        //             depth_stencil: {depth_stencil},
        //             resolve: [],
        //         }
        //     )
        //     .unwrap(),
        // );
        let scene = Arc::new(
            vulkano::ordered_passes_renderpass!(
                device.clone(),
                attachments: {
                    color: {
                        load: Clear,
                        store: Store,
                        format: color_format,
                        samples: 1,
                    },
                    depth_stencil: {
                        load: Clear,
                        store: DontCare,
                        format: framebuffer_attachments.depth.image().format(),
                        samples: 1,
                    }
                },
                passes: [
                    {
                        color: [color],
                        depth_stencil: {depth_stencil},
                        input: [],
                        resolve: [],
                    },
                    {
                        color: [color],
                        depth_stencil: {},
                        input: [],
                        resolve: [],
                    }
                ]
            )
            .unwrap(),
        );
        RenderPasses { /*shadow,*/ scene }
    }

    fn create_pipelines(
        device: &Arc<Device>,
        swap_chain_extent: [u32; 2],
        render_passes: &RenderPasses,
        shader: &Shader,
    ) -> Pipelines {
        let dimensions = [swap_chain_extent[0] as f32, swap_chain_extent[1] as f32];
        let viewport = Viewport {
            origin: [0.0, 0.0],
            dimensions,
            depth_range: 0.0..1.0,
        };

        let builder = VulkanoGraphicsPipeline::start()
            .triangle_list()
            .primitive_restart(false)
            .viewports(vec![viewport])
            .polygon_mode_fill()
            .line_width(1.0)
            .cull_mode_back();

        Pipelines {
            // shadow: Arc::new(
            //     builder
            //         .clone()
            //         .cull_mode_front()
            //         .render_pass(Subpass::from(render_passes.shadow.clone(), 0).unwrap())
            //         .vertex_input_single_buffer::<GpuVertex>()
            //         .vertex_shader(shader.shadow_vert_entry_point(), ())
            //         .fragment_shader(shader.shadow_frag_entry_point(), ())
            //         .build(device.clone())
            //         .unwrap(),
            // ),
            scene: Arc::new(
                builder
                    .clone()
                    .blend_pass_through()
                    .depth_clamp(false)
                    .depth_stencil(DepthStencil {
                        depth_compare: Compare::Less,
                        depth_write: true,
                        depth_bounds_test: DepthBounds::Disabled,
                        stencil_front: Default::default(),
                        stencil_back: Default::default(),
                    })
                    .render_pass(Subpass::from(render_passes.scene.clone(), 0).unwrap())
                    .vertex_input_single_buffer::<Vertex>()
                    .vertex_shader(shader.scene_vert_entry_point(), ())
                    .fragment_shader(shader.scene_frag_entry_point(), ())
                    .build(device.clone())
                    .unwrap(),
            ),
            gui: Arc::new(
                builder
                    .blend_alpha_blending()
                    .render_pass(Subpass::from(render_passes.scene.clone(), 1).unwrap())
                    .vertex_input_single_buffer::<Vertex>()
                    .vertex_shader(shader.scene_vert_entry_point(), ())
                    .fragment_shader(shader.scene_frag_entry_point(), ())
                    .build(device.clone())
                    .unwrap(),
            ),
        }
    }

    fn create_gui_painter(device: &Arc<Device>, queues: &Queues, render_passes: &RenderPasses) -> Painter {
        Painter::new(
            device.clone(),
            queues.graphics.clone(),
            Subpass::from(render_passes.scene.clone(), 1).unwrap(),
        )
        .unwrap()
    }

    fn create_framebuffers(
        swap_chain_images: &[Arc<ImageWithView<SwapchainImage<Window>>>],
        render_passes: &RenderPasses,
        attachments: &FramebufferAttachments,
    ) -> Vec<Framebuffers> {
        swap_chain_images
            .iter()
            .map(|image| Framebuffers {
                // shadow: Arc::new(
                //     VulkanoFramebuffer::start(render_passes.shadow.clone())
                //         .add(attachments.shadow.clone())
                //         .unwrap()
                //         .build()
                //         .unwrap(),
                // ) as Arc<Framebuffer>,
                scene: Arc::new(
                    VulkanoFramebuffer::start(render_passes.scene.clone())
                        .add(image.clone())
                        .unwrap()
                        .add(attachments.depth.clone())
                        .unwrap()
                        .build()
                        .unwrap(),
                ) as Arc<Framebuffer>,
            })
            .collect::<Vec<_>>()
    }

    fn create_texture_samplers(device: &Arc<Device>) -> Samplers {
        Samplers {
            texture: Sampler::new(
                device.clone(),
                Filter::Linear,
                Filter::Linear,
                MipmapMode::Linear,
                SamplerAddressMode::Repeat,
                SamplerAddressMode::Repeat,
                SamplerAddressMode::Repeat,
                0.0,
                16.0,
                0.0,
                100.0,
            )
            .unwrap(),
            pixel_texture: Sampler::new(
                device.clone(),
                Filter::Nearest,
                Filter::Nearest,
                MipmapMode::Nearest,
                SamplerAddressMode::Repeat,
                SamplerAddressMode::Repeat,
                SamplerAddressMode::Repeat,
                0.0,
                1.0,
                0.0,
                100.0,
            )
            .unwrap(),
            shadow: Sampler::new(
                device.clone(),
                Filter::Nearest,
                Filter::Nearest,
                MipmapMode::Nearest,
                SamplerAddressMode::ClampToBorder(BorderColor::FloatOpaqueWhite),
                SamplerAddressMode::ClampToBorder(BorderColor::FloatOpaqueWhite),
                SamplerAddressMode::ClampToBorder(BorderColor::FloatOpaqueWhite),
                0.0,
                1.0,
                0.0,
                100.0,
            )
            .unwrap(),
        }
    }

    fn create_uniform_buffers(device: &Arc<Device>) -> UniformBuffers {
        UniformBuffers {
            camera: CpuBufferPool::uniform_buffer(device.clone()),
            dynamic_buffer_alignment: device.physical_device().limits().min_uniform_buffer_offset_alignment() as usize,
        }
    }

    fn create_descriptor_sets_pool(pipelines: Pipelines) -> DescriptorSetPools {
        DescriptorSetPools {
            // shadow: FixedSizeDescriptorSetsPool::new(pipelines.shadow.descriptor_set_layout(0).unwrap().clone()),
            camera: FixedSizeDescriptorSetsPool::new(pipelines.scene.descriptor_set_layout(0).unwrap().clone()),
            scene: FixedSizeDescriptorSetsPool::new(pipelines.scene.descriptor_set_layout(1).unwrap().clone()),
        }
    }

    pub fn request_recreate_swapchain(&mut self) {
        self.should_recreate_swap_chain = true;
    }
}

pub(super) struct Frame {
    pub re_init: bool,
    pub framebuffers: Framebuffers,
    image_index: usize,
    acquire_future: SwapchainAcquireFuture<Window>,
}

impl VulkanWrapper {
    pub(super) fn begin_frame(&mut self) -> Option<Frame> {
        self.previous_frame_end.as_mut().unwrap().cleanup_finished();
        let re_init = self.should_recreate_swap_chain;
        if self.should_recreate_swap_chain {
            self.should_recreate_swap_chain = false;
            if self.surface.window().inner_size().width > 0 && self.surface.window().inner_size().height > 0 {
                self.recreate_swap_chain();
            }
        }

        let (image_index, _suboptimal, acquire_future) = match acquire_next_image(self.swap_chain.clone(), None) {
            Ok(r) => r,
            Err(AcquireError::OutOfDate) => {
                self.should_recreate_swap_chain = true;
                return None;
            }
            Err(err) => panic!("{:?}", err),
        };

        Some(Frame {
            re_init,
            framebuffers: self.swap_chain_framebuffers[image_index].clone(),
            image_index,
            acquire_future,
        })
    }

    pub(super) fn submit_frame(&mut self, frame: Frame, command_buffer: Arc<PrimaryAutoCommandBuffer>) {
        let future = self
            .previous_frame_end
            .take()
            .unwrap()
            .join(frame.acquire_future)
            .then_execute(self.queues.graphics.clone(), command_buffer)
            .unwrap()
            .then_swapchain_present(self.queues.present.clone(), self.swap_chain.clone(), frame.image_index)
            .then_signal_fence();

        match future.flush() {
            Ok(_) => {
                self.previous_frame_end = Some(Box::new(future) as Box<_>);
            }
            Err(vulkano::sync::FlushError::OutOfDate) => {
                self.should_recreate_swap_chain = true;
                self.previous_frame_end = Some(Box::new(vulkano::sync::now(self.device.clone())) as Box<_>);
            }
            Err(e) => {
                eprintln!("{:?}", e);
                self.previous_frame_end = Some(Box::new(vulkano::sync::now(self.device.clone())) as Box<_>);
            }
        }
    }
}
