use winit::event::{Event, WindowEvent};
use winit::event_loop::{ControlFlow, EventLoop, EventLoopWindowTarget};

use crate::renderer::vulkan::VulkanWrapper;
use crate::renderer::RoastRenderer;

/// Rust version of `ai.arcblroth.boss.RendererSettings`
pub struct RendererSettings {
    pub renderer_size: (f64, f64),
    pub fullscreen_mode: FullscreenMode,
    pub transparent: bool,
}

/// Rust version of `ai.arcblroth.boss.RendererSettings$FullscreenMode`
#[derive(Ord, PartialOrd, Eq, PartialEq)]
pub enum FullscreenMode {
    None,
    Borderless,
    Exclusive,
}

/// The main struct encapsulating all backend logic.
pub struct Roast {
    pub event_loop: EventLoop<()>,
    pub renderer: RoastRenderer,
}

impl Roast {
    pub fn new(app_name: String, _app_version: String, renderer_settings: RendererSettings) -> Self {
        let event_loop = EventLoop::new();
        let vulkan = VulkanWrapper::new(&event_loop, app_name, renderer_settings);
        let renderer = RoastRenderer::new(vulkan);
        Self { event_loop, renderer }
    }

    pub fn run_event_loop(self) -> ! {
        let mut renderer = self.renderer;
        renderer.vulkan.surface.window().set_visible(true);
        self.event_loop.run(
            move |event: Event<_>, _event_loop_target: &EventLoopWindowTarget<_>, control_flow: &mut ControlFlow| {
                *control_flow = ControlFlow::Poll;

                match event {
                    Event::WindowEvent {
                        event: WindowEvent::CloseRequested,
                        ..
                    } => {
                        *control_flow = ControlFlow::Exit;
                    }
                    Event::WindowEvent {
                        event: WindowEvent::Resized(_),
                        ..
                    } => {
                        renderer.vulkan.request_recreate_swapchain();
                    }
                    Event::MainEventsCleared => {
                        renderer.render();
                    }
                    _ => (),
                }
            },
        );
    }
}
