//! # Roast
//! The winit + vulkano Backend for Bosstrove's Revenge
//! Implemented in Rust because I need to improve my Rust skills
//! and don't wanna import all of LWJGL
//!
//! Or quote [the docs][1] for jni-rs:
//! > Because who wants to _actually_ write Java?
//!
//! [1]: https://docs.rs/jni/0.19.0/jni/
use std::sync::atomic::{AtomicBool, Ordering};

use jni::sys::{jobject, jstring};
use jni::JNIEnv;
use log::info;

use crate::logger::JavaLogger;

pub mod jni_util;
pub mod logger;

/// Catches any unwinding panics and rethrows them as a Java exception
macro_rules! catch_panic {
    ($env:expr, $code:block) => {
        if let Err(err) = std::panic::catch_unwind(|| $code) {
            let _ = $env.throw_new("ai/arcblroth/boss/desktop/RoastException", "panic!");
        }
    };
}

static JNI_INIT_LOCK: AtomicBool = AtomicBool::new(false);

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
        if let Ok(_) =
            JNI_INIT_LOCK.compare_exchange(false, true, Ordering::SeqCst, Ordering::SeqCst)
        {
            std::env::set_var("RUST_BACKTRACE", "full");
            let logger = unwrap_or_throw_new!(
                JavaLogger::new(env),
                env,
                "Could not construct backend logger"
            );
            unwrap_or_throw_new!(logger.init(), env, "Could not initialize backend logger");
        }

        info!("Let the roasting begin!");
    });
}
