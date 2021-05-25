//! # Roast
//! The winit + vulkano Backend for Bosstrove's Revenge
//! Implemented in Rust because I need to improve my Rust skills
//! and don't wanna import all of LWJGL
//!
//! Or quote [the docs][1] for jni-rs:
//! > Because who wants to _actually_ write Java?
//!
//! [1]: https://docs.rs/jni/0.19.0/jni/

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jobject, jstring};

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_desktop_RoastBackend_init(
    env: JNIEnv,
    this: jobject,
    app_name: jstring,
    app_version: jstring,
    renderer_settings: jobject,
) {
    println!("Let the roasting begin!");
}
