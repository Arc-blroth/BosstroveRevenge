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

use std::cell::RefCell;
use std::collections::HashMap;
use std::sync::atomic::{AtomicBool, Ordering};

use jni::objects::{JObject, JString, JValue, ReleaseMode};
use jni::signature::{JavaType, Primitive};
use jni::sys::{jintArray, jobject, jobjectArray, jstring};
use jni::JNIEnv;

use crate::backend::{FullscreenMode, RendererSettings, Roast};
use crate::jni_util::Unboxing;
use crate::logger::JavaLogger;
use crate::renderer::mesh::Mesh;
use crate::renderer::scene::Scene;
use crate::renderer::shader::{Vertex, VertexType};
use crate::renderer::MeshId;

pub mod backend;
pub mod jni_util;
pub mod logger;
pub mod renderer;

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
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastBackend_init(
    env: JNIEnv<'static>,
    this: jobject,
    app_name: jstring,
    app_version: jstring,
    renderer_settings: jobject,
) {
    catch_panic!(env, {
        // SAFETY: Thread safety guaranteed by Atomics™
        // Note that if initialization panics then things will break.
        if let Ok(_) = JNI_INIT_LOCK.compare_exchange(false, true, Ordering::SeqCst, Ordering::SeqCst) {
            std::env::set_var("RUST_BACKTRACE", "full");
            let logger = unwrap_or_throw_new!(JavaLogger::new(env), env, "Could not construct backend logger");
            unwrap_or_throw_new!(logger.init(), env, "Could not initialize backend logger");
        }

        let app_name = env.get_string(JString::from(app_name)).unwrap().into();
        let app_version = env.get_string(JString::from(app_version)).unwrap().into();
        let renderer_settings = JObject::from(renderer_settings);
        let renderer_size = call_getter!(env, renderer_settings, "getRendererSize", "Lkotlin/Pair;")
            .l()
            .unwrap();
        let renderer_size = (
            call_getter!(env, renderer_size, "getFirst", "Ljava/lang/Object;")
                .unbox_double(&env)
                .unwrap(),
            call_getter!(env, renderer_size, "getSecond", "Ljava/lang/Object;")
                .unbox_double(&env)
                .unwrap(),
        );
        let fullscreen_mode = call_getter!(
            env,
            renderer_settings,
            "getFullscreenMode",
            "Lai/arcblroth/boss/RendererSettings$FullscreenMode;"
        )
        .l()
        .unwrap();
        let fullscreen_mode = match call_getter!(env, fullscreen_mode, "ordinal", "I").i().unwrap() {
            1 => FullscreenMode::Exclusive,
            2 => FullscreenMode::Borderless,
            _ => FullscreenMode::None,
        };
        let transparent = call_getter!(env, renderer_settings, "getTransparent", "Z").z().unwrap();
        let renderer_settings = RendererSettings {
            renderer_size,
            fullscreen_mode,
            transparent,
        };

        let default_texture = crate::renderer::texture::generate_default_texture(env);

        BACKEND_STORAGE_KEY_COUNTER.with(|counter_cell| {
            let mut counter = counter_cell.borrow_mut();
            *counter = counter
                .checked_add(1)
                .expect("this is not okay (Backend storage overflow)");
            BACKEND_STORAGE.with(|storage_cell| {
                let mut storage = storage_cell.borrow_mut();
                storage.insert(
                    *counter,
                    Roast::new(app_name, app_version, renderer_settings, default_texture),
                );
            });
            env.set_field(this, "pointer", "J", JValue::Long(*counter as i64))
                .unwrap();
        });

        log::info!("Let the roasting begin!");
    });
}

/// Checks if the backend pointer from `this` refers to a non-null
/// and existing backend. On success, returns the pointer as a u64.
/// On error, throws a Java exception and returns `Err`.
fn check_backend(env: &JNIEnv, this: jobject) -> Result<u64, ()> {
    let pointer = env.get_field(this, "pointer", "J").unwrap().j().unwrap() as u64;
    if pointer == 0 {
        env.throw_new("java/lang/NullPointerException", "Backend pointer is null!")
            .unwrap();
        return Err(());
    }
    CURRENT_BACKEND.with(|current_backend_cell| {
        match *current_backend_cell.borrow() {
            None => {
                // No backend is running, so check storage
                BACKEND_STORAGE.with(|storage_cell| {
                    if storage_cell.borrow().contains_key(&pointer) {
                        Ok(pointer)
                    } else {
                        env.throw_new(
                            "java/lang/IllegalStateException",
                            "Backend pointer does not point to a valid struct",
                        )
                        .unwrap();
                        Err(())
                    }
                })
            }
            Some(running_pointer) => {
                // A backend is currently running, so check
                // if the pointer matches
                if pointer == running_pointer {
                    Ok(pointer)
                } else {
                    env.throw_new(
                        "java/lang/IllegalStateException",
                        "Only one backend can run its event loop at a time",
                    )
                    .unwrap();
                    Err(())
                }
            }
        }
    })
}

/// Checks if the backend pointer from `this` is valid, and if so runs
/// the callback with the backend as its only argument, then returns `Ok`.
fn with_backend<F, R>(env: &JNIEnv, this: jobject, callback: F) -> Result<R, ()>
where
    F: FnOnce(Roast) -> R,
{
    let pointer = check_backend(env, this)?;
    BACKEND_STORAGE.with(|storage_cell| {
        let mut storage = storage_cell.borrow_mut();
        match storage.remove(&pointer) {
            None => {
                // We can only get here if init() is called on a valid, running backend
                env.throw_new("java/lang/IllegalStateException", "Cannot initialize backend twice")
                    .unwrap();
                Err(())
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
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastBackend_runEventLoop(
    env: JNIEnv<'static>,
    this: jobject,
    step: jobject,
) -> jobject {
    catch_panic!(env, {
        let step = JObject::from(step);
        let step_class = env.get_object_class(step).unwrap();
        let invoke_method = env
            .get_method_id(step_class, "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;")
            .unwrap();
        let env_for_closure = env.clone();
        let args = [JValue::Object(JObject::from(this))];
        let invoke_step = move || {
            env_for_closure
                .call_method_unchecked(
                    step,
                    invoke_method,
                    JavaType::Object("Ljava/lang/Object;".to_string()),
                    &args,
                )
                .expect("Failed to invoke step callback");
        };

        with_backend(&env, this, |backend| backend.run_event_loop(invoke_step))
    });
    JObject::null().into_inner()
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastBackend_createMesh(
    env: JNIEnv,
    this: jobject,
    vertices: jobjectArray,
    indices: jintArray,
    vertex_type: jobject,
    texture0: jobject,
    texture1: jobject,
) -> jobject {
    catch_panic!(env, {
        check_backend(&env, this).unwrap();

        const VECTOR3F_CLASS: &str = "ai/arcblroth/boss/math/Vector3f";
        const VECTOR4F_CLASS: &str = "ai/arcblroth/boss/math/Vector4f";
        const VECTOR3F_TYPE: &str = "Lai/arcblroth/boss/math/Vector3f;";
        const VECTOR4F_TYPE: &str = "Lai/arcblroth/boss/math/Vector4f;";

        let vertex_class = env.find_class("ai/arcblroth/boss/render/Vertex").unwrap();
        let vertex_pos = env.get_field_id(vertex_class, "pos", VECTOR3F_TYPE).unwrap();
        let vertex_color_tex = env.get_field_id(vertex_class, "colorTex", VECTOR4F_TYPE).unwrap();

        let vector3f_class = env.find_class(VECTOR3F_CLASS).unwrap();
        let vector3f_x = env.get_field_id(vector3f_class, "x", "F").unwrap();
        let vector3f_y = env.get_field_id(vector3f_class, "y", "F").unwrap();
        let vector3f_z = env.get_field_id(vector3f_class, "z", "F").unwrap();

        let vector4f_class = env.find_class(VECTOR4F_CLASS).unwrap();
        let vector4f_x = env.get_field_id(vector4f_class, "x", "F").unwrap();
        let vector4f_y = env.get_field_id(vector4f_class, "y", "F").unwrap();
        let vector4f_z = env.get_field_id(vector4f_class, "z", "F").unwrap();
        let vector4f_w = env.get_field_id(vector4f_class, "w", "F").unwrap();

        let get_vertex = |obj: JObject| -> Vertex {
            let pos = env.get_field_unchecked(obj, vertex_pos, JavaType::Object(VECTOR3F_TYPE.to_string())).unwrap().l().unwrap();
            let pos = [
                env.get_field_unchecked(pos, vector3f_x, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
                env.get_field_unchecked(pos, vector3f_y, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
                env.get_field_unchecked(pos, vector3f_z, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
            ];
            let color_tex = env.get_field_unchecked(obj, vertex_color_tex, JavaType::Object(VECTOR4F_TYPE.to_string())).unwrap().l().unwrap();
            let color_tex = [
                env.get_field_unchecked(color_tex, vector4f_x, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
                env.get_field_unchecked(color_tex, vector4f_y, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
                env.get_field_unchecked(color_tex, vector4f_z, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
                env.get_field_unchecked(color_tex, vector4f_w, JavaType::Primitive(Primitive::Float)).unwrap().f().unwrap(),
            ];
            Vertex {
                pos,
                color_tex,
            }
        };

        let vertices_len = env.get_array_length(vertices).unwrap();
        let mut rust_vertices = Vec::with_capacity(vertices_len as usize);
        for i in 0..vertices_len {
            rust_vertices.push(get_vertex(env.get_object_array_element(vertices, i).unwrap()));
        }

        let indices_array = env.get_int_array_elements(indices, ReleaseMode::NoCopyBack).unwrap();
        let rust_indices = unsafe {
            std::slice::from_raw_parts(indices_array.as_ptr() as *const u32, indices_array.size().unwrap() as usize)
        };

        let rust_vertex_type = match call_getter!(env, vertex_type, "ordinal", "I").i().unwrap() {
            0 => VertexType::COLOR,
            1 => VertexType::TEX1,
            2 => VertexType::TEX2,
            _ => {
                env.throw_new("java/lang/IllegalArgumentException", "Invalid vertex type!").unwrap();
                panic!();
            }
        };

        let get_texture = |obj| {
            if obj == std::ptr::null_mut() {
                None
            } else {
                unimplemented!();
            }
        };

        let rust_texture0 = get_texture(texture0);
        let rust_texture1 = get_texture(texture1);

        let out_pointer = backend::with_renderer(move |renderer| {
            let mesh = Mesh::build(
                rust_vertices.as_slice(),
                rust_indices,
                rust_vertex_type,
                rust_texture0,
                rust_texture1,
                &renderer.vulkan,
            );
            renderer.register_mesh(mesh)
        });

        env.new_object("ai/arcblroth/boss/roast/RoastMesh", "(J)V", &[JValue::Long(out_pointer as i64)]).unwrap().into_inner()
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastBackend_render(env: JNIEnv, this: jobject, scene: jobject) {
    #[inline]
    fn get_mesh_array_from_scene(env: JNIEnv, scene: jobject, array_list_getter: &str) -> Vec<MeshId> {
        let meshes = call_getter!(env, scene, array_list_getter, "Ljava/util/ArrayList;")
            .l()
            .unwrap();
        let len = call_getter!(env, meshes, "size", "I").i().unwrap();
        let array_obj = env
            .get_field(meshes, "elementData", "[Ljava/lang/Object;")
            .unwrap()
            .l()
            .unwrap()
            .into_inner();

        let mesh_class = env.find_class("ai/arcblroth/boss/roast/RoastMesh").unwrap();
        let mesh_pointer_field = env.get_field_id(mesh_class, "pointer", "J").unwrap();

        let mut out = Vec::with_capacity(len as usize);
        for i in 0..len {
            let mesh = env.get_object_array_element(array_obj, i).unwrap();
            out.push(
                env.get_field_unchecked(mesh, mesh_pointer_field, JavaType::Primitive(Primitive::Long))
                    .unwrap()
                    .j()
                    .unwrap() as MeshId,
            );
        }
        out
    }

    catch_panic!(env, {
        check_backend(&env, this).unwrap();

        let scene_meshes = get_mesh_array_from_scene(env, scene, "getSceneMeshes");
        let gui_meshes = get_mesh_array_from_scene(env, scene, "getGuiMeshes");
        let scene = Scene {
            scene_meshes,
            gui_meshes,
        };

        backend::with_renderer(move |renderer| {
            renderer.render(scene);
        });
    });
}
