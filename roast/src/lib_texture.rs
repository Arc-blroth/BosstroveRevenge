//! JNI implementation of the `ai.arcblroth.roast.RoastTexture` native methods.

use jni::objects::JObject;
use jni::sys::{jboolean, jint, jobject, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;

use crate::backend;
use crate::jni_classes::JavaTextureSampling;

const TEXTURE_NOT_FOUND_MSG: &str = "Texture pointer does not point to a valid texture";

fn get_texture_pointer(env: JNIEnv, this: jobject) -> u64 {
    env.get_field(this, "pointer", "J").unwrap().j().unwrap() as u64
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastTexture_getWidth(env: JNIEnv, this: jobject) -> jint {
    catch_panic!(env, {
        let pointer = get_texture_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.textures.get(&pointer).expect(TEXTURE_NOT_FOUND_MSG).width() as i32
        })
    } else {
        0
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastTexture_getHeight(env: JNIEnv, this: jobject) -> jint {
    catch_panic!(env, {
        let pointer = get_texture_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.textures.get(&pointer).expect(TEXTURE_NOT_FOUND_MSG).height() as i32
        })
    } else {
        0
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastTexture_getTextureSampling(
    env: JNIEnv,
    this: jobject,
) -> jobject {
    catch_panic!(env, {
        let texture_sampling_class = JavaTextureSampling::accessor(env);
        let pointer = get_texture_pointer(env, this);

        texture_sampling_class.to_java(
            backend::with_renderer(move |renderer| {
                renderer.textures.get(&pointer).expect(TEXTURE_NOT_FOUND_MSG).sampling()
            })
        )
        .into_inner()
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastTexture_getMipmapped(env: JNIEnv, this: jobject) -> jboolean {
    catch_panic!(env, {
        let pointer = get_texture_pointer(env, this);

        backend::with_renderer(move |renderer| {
            if renderer.textures.get(&pointer).expect(TEXTURE_NOT_FOUND_MSG).mipmapped() {
                JNI_TRUE
            } else {
                JNI_FALSE
            }
        })
    } else {
        JNI_FALSE
    });
}
