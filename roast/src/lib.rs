//! # Roast
//! The winit + vulkano Backend for Bosstrove's Revenge
//!
//! Implemented in Rust because I need to improve my Rust skills
//! and don't wanna import all of LWJGL
//!
//! Or quote [the docs][1] for jni-rs:
//! > Because who wants to _actually_ write Java?
//!
//! [1]: https://docs.rs/jni/0.19.0/jni/

#![feature(array_map)]
#![feature(label_break_value)]
#![feature(vec_into_raw_parts)]

use std::cell::RefCell;
use std::collections::HashMap;
use std::sync::atomic::{AtomicBool, Ordering};

use anyhow::{bail, Context, Result};
use glam::DVec2;

use crate::backend::{RendererSettings, Roast};
use crate::error::{catch_panic, ForeignRoastResult, Nothing, RoastError};
use crate::ffi::util::{slice_from_foreign, string_from_foreign, ForeignOption};
use crate::logger::{JavaLogger, JavaLoggerCallbacks};
use crate::renderer::camera::Camera;
use crate::renderer::mesh::Mesh;
use crate::renderer::scene::Scene;
use crate::renderer::shader::{Vertex, VertexType};
use crate::renderer::texture::{Texture, TextureSampling};
use crate::renderer::{MeshId, TextureId};
use crate::texture::{JavaDefaultTextureNumbers, DEFAULT_TEXTURE_NUMBERS_LEN};

pub mod backend;
pub mod error;
pub mod ffi;
pub mod logger;
pub mod mesh;
mod ogt_util;
pub mod renderer;
pub mod texture;
pub mod ui;

/// JNI initialization lock. This prevents the backend logger
/// from being initialized twice.
static JNI_INIT_LOCK: AtomicBool = AtomicBool::new(false);

thread_local! {
    /// The doc for Backend explicitly mentions that Backends should
    /// never be initialized on more than one thread. Thus, the Backend
    /// storage map is implemented as a thread local.
    ///
    /// Note that it is only possible to ever run one backend, so
    /// initializing multiple is kinda useless.
    static BACKEND_STORAGE: RefCell<HashMap<u64, Roast>> = RefCell::new(HashMap::new());

    /// Key generator for putting backends in storage. Right now
    /// this is just a u64 that counts upwards.
    static BACKEND_STORAGE_KEY_COUNTER: RefCell<u64> = RefCell::new(0);

    /// Stores the pointer key of the backend currently in `runEventLoop`.
    static CURRENT_BACKEND: RefCell<Option<u64>> = RefCell::new(None);
}

#[no_mangle]
pub extern "C" fn roast_backend_init(
    logger_callbacks: JavaLoggerCallbacks,
    default_texture_numbers: JavaDefaultTextureNumbers,
    app_name: *const u8,
    app_name_len: usize,
    app_version: *const u8,
    app_version_len: usize,
    renderer_settings: RendererSettings,
) -> ForeignRoastResult<u64> {
    catch_panic(move || {
        if let Ok(_) = JNI_INIT_LOCK.compare_exchange(false, true, Ordering::SeqCst, Ordering::SeqCst) {
            std::env::set_var("RUST_BACKTRACE", "full");
            JavaLogger::new(logger_callbacks)
                .init()
                .context("Could not initialize backend logger")?;
        }

        let app_name = string_from_foreign(app_name, app_name_len)?;
        let app_version = string_from_foreign(app_version, app_version_len)?;
        let default_texture_numbers = slice_from_foreign(default_texture_numbers, DEFAULT_TEXTURE_NUMBERS_LEN)?;

        let default_texture = texture::generate_default_texture(default_texture_numbers);

        BACKEND_STORAGE_KEY_COUNTER.with(|counter_cell| {
            let mut counter = counter_cell.borrow_mut();
            *counter = counter
                .checked_add(1)
                .context("this is not okay (Backend storage overflow)")?;
            BACKEND_STORAGE.with(|storage_cell| {
                let mut storage = storage_cell.borrow_mut();
                storage.insert(
                    *counter,
                    Roast::new(app_name, app_version, renderer_settings, default_texture),
                );
            });
            log::info!("Let the roasting begin!");
            Ok(*counter)
        })
    })
}

/// Checks if the backend pointer from `this` refers to a non-null
/// and existing backend. On success, returns the pointer as a u64.
/// On error, throws a Java exception and returns `Err`.
pub fn check_backend(this: u64) -> Result<u64> {
    if this == 0 {
        bail!(RoastError::NullPointer("Backend pointer is null!".to_string()));
    }
    CURRENT_BACKEND.with(|current_backend_cell| {
        match *current_backend_cell.borrow() {
            None => {
                // No backend is running, so check storage
                BACKEND_STORAGE.with(|storage_cell| {
                    if storage_cell.borrow().contains_key(&this) {
                        Ok(this)
                    } else {
                        bail!(RoastError::IllegalState(
                            "Backend pointer does not point to a valid struct".to_string()
                        ));
                    }
                })
            }
            Some(running_pointer) => {
                // A backend is currently running, so check
                // if the pointer matches
                if this == running_pointer {
                    Ok(this)
                } else {
                    bail!(RoastError::IllegalState(
                        "Only one backend can run its event loop at a time".to_string()
                    ));
                }
            }
        }
    })
}

/// Checks if the backend pointer from `this` is valid, and if so runs
/// the callback with the backend as its only argument, then returns `Ok`.
fn with_backend<F, R>(this: u64, callback: F) -> Result<R>
where
    F: FnOnce(Roast) -> R,
{
    let pointer = check_backend(this)?;
    BACKEND_STORAGE.with(|storage_cell| {
        let mut storage = storage_cell.borrow_mut();
        match storage.remove(&pointer) {
            None => {
                // We can only get here if init() is called on a valid, running backend
                bail!(RoastError::IllegalState("Cannot initialize backend twice".to_string()));
            }
            Some(roast) => {
                CURRENT_BACKEND.with(|current_backend_cell| {
                    *current_backend_cell.borrow_mut() = Some(pointer);
                });
                Ok(callback(roast))
            }
        }
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_run_event_loop(this: u64, step: extern "C" fn() -> ()) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        with_backend(this, |backend| backend.run_event_loop(move || step()))?;
        Ok(Nothing::default())
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_create_texture(
    this: u64,
    image: *const u8,
    image_len: usize,
    sampling: TextureSampling,
    generate_mipmaps: bool,
) -> ForeignRoastResult<TextureId> {
    catch_panic(move || {
        check_backend(this)?;

        let rust_image =
            image::load_from_memory(slice_from_foreign(image, image_len)?).context("Could not load image!")?;

        backend::with_renderer(move |renderer| {
            let texture = Texture::new(&renderer.vulkan, rust_image, sampling, generate_mipmaps);
            Ok(renderer.register_texture(texture))
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_create_mesh(
    this: u64,
    vertices: *const Vertex,
    vertices_len: usize,
    indices: *const u32,
    indices_len: usize,
    vertex_type: VertexType,
    texture0: ForeignOption<TextureId>,
    texture1: ForeignOption<TextureId>,
) -> ForeignRoastResult<MeshId> {
    catch_panic(move || {
        check_backend(this)?;

        let rust_vertices = slice_from_foreign(vertices, vertices_len)?;
        let rust_indices = slice_from_foreign(indices, indices_len)?;

        backend::with_renderer(move |renderer| {
            let mesh = Mesh::build(
                rust_vertices,
                rust_indices,
                vertex_type,
                texture0.into(),
                texture1.into(),
                &renderer.vulkan,
            );
            Ok(renderer.register_mesh(mesh))
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_create_mesh_from_vox(
    this: u64,
    vox: *const u8,
    vox_len: usize,
) -> ForeignRoastResult<MeshId> {
    catch_panic(move || {
        check_backend(this)?;

        let rust_vox = slice_from_foreign(vox, vox_len)?;
        let (vertices, indices) = ogt_util::meshify_voxel(rust_vox);

        backend::with_renderer(move |renderer| {
            let mesh = Mesh::build(
                vertices.as_slice(),
                indices.as_slice(),
                VertexType::COLOR,
                None,
                None,
                &renderer.vulkan,
            );
            Ok(renderer.register_mesh(mesh))
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_create_mesh_with_geometry(this: u64, geometry: MeshId) -> ForeignRoastResult<MeshId> {
    catch_panic(move || {
        check_backend(this)?;

        backend::with_renderer(move |renderer| {
            let geometry_mesh = renderer.meshes.get(&geometry).context(mesh::MESH_NOT_FOUND_MSG)?;
            let mesh = Mesh::with_geometry(geometry_mesh);
            Ok(renderer.register_mesh(mesh))
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_get_size(this: u64) -> ForeignRoastResult<DVec2> {
    catch_panic(move || {
        check_backend(this)?;

        let size = backend::with_renderer(|renderer| renderer.vulkan.surface.window().inner_size());

        Ok(DVec2::new(size.width as f64, size.height as f64))
    })
}

#[no_mangle]
pub extern "C" fn roast_backend_render(
    this: u64,
    camera: Camera,
    scene_meshes: *const MeshId,
    scene_meshes_len: usize,
    gui_meshes: *const MeshId,
    gui_meshes_len: usize,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        check_backend(this)?;

        let rust_scene_meshes = slice_from_foreign(scene_meshes, scene_meshes_len)?;
        let rust_gui_meshes = slice_from_foreign(gui_meshes, gui_meshes_len)?;
        let scene = Scene {
            camera,
            scene_meshes: rust_scene_meshes,
            gui_meshes: rust_gui_meshes,
        };

        backend::with_renderer(move |renderer| {
            // begin_frame is called in the main event loop in backend.rs
            let gui_data = renderer.gui.end_frame();
            renderer.render(scene, gui_data.1);
        });

        Ok(Nothing::default())
    })
}
