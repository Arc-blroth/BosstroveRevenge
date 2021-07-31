use std::cell::RefCell;

use glam::DVec2;
use image::DynamicImage;
use winit::event::{Event, WindowEvent};
use winit::event_loop::{ControlFlow, EventLoop, EventLoopWindowTarget};

use crate::renderer::vulkan::VulkanWrapper;
use crate::renderer::RoastRenderer;

/// Rust version of `ai.arcblroth.boss.RendererSettings`
#[repr(C)]
pub struct RendererSettings {
    pub renderer_size: DVec2,
    pub fullscreen_mode: FullscreenMode,
    pub transparent: bool,
}

/// Rust version of `ai.arcblroth.boss.RendererSettings$FullscreenMode`
#[repr(C)]
#[derive(Ord, PartialOrd, Eq, PartialEq)]
pub enum FullscreenMode {
    None,
    Borderless,
    Exclusive,
}

thread_local! {
    /// Thread local storage for the active backend renderer.
    static RENDERER: RefCell<Option<RoastRenderer>> = RefCell::new(None);

    /// This is set by EventLoop.exit and checked in the event loop.
    static SHOULD_STOP: RefCell<bool> = RefCell::new(false);
}

/// Mutably borrows the thread-local renderer.
///
/// # Panics
/// This function will panic if the renderer has not yet
/// been initialized or if the renderer is being mutably
/// borrowed somewhere else at the same time.
pub fn with_renderer<F, R>(f: F) -> R
where
    F: FnOnce(&mut RoastRenderer) -> R,
{
    RENDERER.with(|renderer_cell| f(renderer_cell.borrow_mut().as_mut().unwrap()))
}

/// The main struct encapsulating all backend logic.
pub struct Roast {
    pub event_loop: EventLoop<()>,
    pub renderer: RoastRenderer,
}

impl Roast {
    pub fn new(
        app_name: String,
        _app_version: String,
        renderer_settings: RendererSettings,
        default_texture: DynamicImage,
    ) -> Self {
        let event_loop = EventLoop::new();
        let vulkan = VulkanWrapper::new(&event_loop, app_name, renderer_settings);
        let renderer = RoastRenderer::new(vulkan, default_texture);
        Self { event_loop, renderer }
    }

    pub fn run_event_loop<S>(self, step: S) -> !
    where
        S: 'static + Fn() -> (),
    {
        let renderer = self.renderer;
        renderer.init();

        // Move the renderer into the thread-local storage so JNI
        // calls can easily find it.
        RENDERER.with(move |renderer_cell| {
            renderer_cell.replace_with(|_| Some(renderer));
        });

        self.event_loop.run(
            move |event: Event<_>, _event_loop_target: &EventLoopWindowTarget<_>, control_flow: &mut ControlFlow| {
                *control_flow = ControlFlow::Poll;
                let mut should_stop = false;

                with_renderer(|renderer| {
                    renderer.gui.handle_event(&event);
                });

                match event {
                    Event::WindowEvent {
                        event: WindowEvent::CloseRequested,
                        ..
                    } => {
                        should_stop = true;
                    }
                    Event::WindowEvent {
                        event: WindowEvent::Resized(_),
                        ..
                    } => {
                        with_renderer(|renderer| {
                            renderer.vulkan.request_recreate_swapchain();
                        });
                    }
                    Event::MainEventsCleared => {
                        with_renderer(|renderer| {
                            // end_frame is called in RoastBackend.render
                            renderer.gui.begin_frame();
                        });
                        step();
                    }
                    _ => (),
                }

                if should_stop {
                    *control_flow = ControlFlow::Exit;
                }
            },
        );
    }
}
