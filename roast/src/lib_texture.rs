//! JNI implementation of the `ai.arcblroth.roast.RoastTexture` native methods.

use jni::objects::JObject;
use jni::sys::{jboolean, jobject, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;

use crate::renderer::texture::TextureSampling;
use crate::{backend, TEXTURE_SAMPLING_CLASS};

const TEXTURE_NOT_FOUND_MSG: &str = "Texture pointer does not point to a valid texture";

fn get_texture_pointer(env: JNIEnv, this: jobject) -> u64 {
    env.get_field(this, "pointer", "J").unwrap().j().unwrap() as u64
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastTexture_getTextureSampling(
    env: JNIEnv,
    this: jobject,
) -> jobject {
    catch_panic!(env, {
        let texture_sampling_class = env.find_class(TEXTURE_SAMPLING_CLASS).unwrap();
        let pointer = get_texture_pointer(env, this);

        let field_name = backend::with_renderer(move |renderer| {
            match renderer.textures.get(&pointer).expect(TEXTURE_NOT_FOUND_MSG).sampling() {
                TextureSampling::Smooth => "SMOOTH",
                TextureSampling::Pixel => "PIXEL",
            }
        });

        env.get_static_field(texture_sampling_class, field_name, TEXTURE_NOT_FOUND_MSG)
            .unwrap()
            .l()
            .unwrap()
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
