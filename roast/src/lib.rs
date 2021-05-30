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

use std::cell::RefCell;
use std::collections::HashMap;
use std::sync::atomic::{AtomicBool, Ordering};

use jni::objects::{JObject, JString, JValue};
use jni::sys::{jobject, jstring};
use jni::JNIEnv;

use crate::backend::{FullscreenMode, RendererSettings, Roast};
use crate::jni_util::Unboxing;
use crate::logger::JavaLogger;

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
pub extern "system" fn Java_ai_arcblroth_boss_desktop_RoastBackend_init(
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

        BACKEND_STORAGE_KEY_COUNTER.with(|counter_cell| {
            let mut counter = counter_cell.borrow_mut();
            *counter = counter
                .checked_add(1)
                .expect("this is not okay (Backend storage overflow)");
            BACKEND_STORAGE.with(|storage_cell| {
                let mut storage = storage_cell.borrow_mut();
                storage.insert(*counter, Roast::new(app_name, app_version, renderer_settings));
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
pub extern "system" fn Java_ai_arcblroth_boss_desktop_RoastBackend_runEventLoop(
    env: JNIEnv,
    this: jobject,
    step: jobject,
) -> jobject {
    catch_panic!(env, { with_backend(&env, this, |backend| backend.run_event_loop()) });
    JObject::null().into_inner()
}
