//! JNI implementation of the `ai.arcblroth.roast.RoastMesh` native methods.

use glam::{Mat4, Vec2, Vec4};
use jni::descriptors::Desc;
use jni::objects::{JObject, JValue};
use jni::signature::{JavaType, Primitive};
use jni::sys::{jfloat, jobject};
use jni::JNIEnv;

use crate::renderer::shader::VertexType;
use crate::renderer::TextureId;
use crate::{
    backend, MATRIX4F_CLASS, OBJECT_TYPE, PAIR_CLASS, ROAST_TEXTURE_CLASS, VECTOR2F_CLASS, VECTOR4F_CLASS,
    VERTEX_TYPE_CLASS, VERTEX_TYPE_TYPE,
};

const MESH_NOT_FOUND_MSG: &str = "Mesh pointer does not point to a valid struct";

fn get_mesh_pointer(env: JNIEnv, this: jobject) -> u64 {
    env.get_field(this, "pointer", "J").unwrap().j().unwrap() as u64
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_getVertexType(env: JNIEnv, this: jobject) -> jobject {
    catch_panic!(env, {
        let vertex_type_class = env.find_class(VERTEX_TYPE_CLASS).unwrap();
        let pointer = get_mesh_pointer(env, this);

        let field_name = backend::with_renderer(move |renderer| {
            match renderer.meshes.get(&pointer).expect(MESH_NOT_FOUND_MSG).vertex_type() {
                VertexType::COLOR => "COLOR",
                VertexType::TEX1 => "TEX1",
                VertexType::TEX2 => "TEX2",
            }
        });

        env.get_static_field(vertex_type_class, field_name, VERTEX_TYPE_TYPE).unwrap().l().unwrap().into_inner()
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_getTextures(env: JNIEnv, this: jobject) -> jobject {
    catch_panic!(env, {
        let roast_texture_class = env.find_class(ROAST_TEXTURE_CLASS).unwrap();
        let roast_texture_ctor = (roast_texture_class, "(J)V").lookup(&env).unwrap();
        let new_roast_texture = move |texture: Option<TextureId>| {
            JValue::Object(match texture {
                Some(id) => env.new_object_unchecked(
                    roast_texture_class,
                    roast_texture_ctor,
                    &[JValue::Long(id as i64)]
                )
                .unwrap(),
                None => JObject::null(),
            })
        };
        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            let textures = renderer.meshes.get(&pointer).expect(MESH_NOT_FOUND_MSG).textures;
            let args = [new_roast_texture(textures.0), new_roast_texture(textures.1)];
            env.new_object(PAIR_CLASS, "(Ljava/lang/Object;Ljava/lang/Object;)V", &args).unwrap().into_inner()
        })
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_setTextures(
    env: JNIEnv,
    this: jobject,
    textures: jobject,
) {
    catch_panic!(env, {
        let pair_class = env.find_class(PAIR_CLASS).unwrap();
        let roast_texture_class = env.find_class(ROAST_TEXTURE_CLASS).unwrap();
        let pair_first_field = env.get_field_id(pair_class, "first", OBJECT_TYPE).unwrap();
        let pair_second_field = env.get_field_id(pair_class, "second", OBJECT_TYPE).unwrap();
        let texture_pointer_field = env.get_field_id(roast_texture_class, "pointer", "J").unwrap();

        let texture0 = env
            .get_field_unchecked(textures, pair_first_field, JavaType::Object(OBJECT_TYPE.to_string()))
            .unwrap()
            .l()
            .unwrap();
        let texture1 = env
            .get_field_unchecked(textures, pair_second_field, JavaType::Object(OBJECT_TYPE.to_string()))
            .unwrap()
            .l()
            .unwrap();

        let texture0_id = if !texture0.is_null() {
            Some(
                env.get_field_unchecked(texture0, texture_pointer_field, JavaType::Primitive(Primitive::Long))
                    .unwrap()
                    .j()
                    .unwrap() as u64,
            )
        } else {
            None
        };
        let texture1_id = if !texture1.is_null() {
            Some(
                env.get_field_unchecked(texture1, texture_pointer_field, JavaType::Primitive(Primitive::Long))
                    .unwrap()
                    .j()
                    .unwrap() as u64,
            )
        } else {
            None
        };

        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.meshes.get_mut(&pointer).unwrap().textures = (texture0_id, texture1_id);
        });
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_getTransform(env: JNIEnv, this: jobject) -> jobject {
    catch_panic!(env, {
        let matrix4f_class = env.find_class(MATRIX4F_CLASS).unwrap();
        let matrix4f_ctor = (matrix4f_class, "(FFFFFFFFFFFFFFFF)V").lookup(&env).unwrap();

        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            let transform = renderer.meshes.get(&pointer).expect(MESH_NOT_FOUND_MSG).transform;
            env.new_object_unchecked(
                matrix4f_class,
                matrix4f_ctor,
                &[
                    JValue::Float(transform.x_axis.x),
                    JValue::Float(transform.x_axis.y),
                    JValue::Float(transform.x_axis.z),
                    JValue::Float(transform.x_axis.w),
                    JValue::Float(transform.y_axis.x),
                    JValue::Float(transform.y_axis.y),
                    JValue::Float(transform.y_axis.z),
                    JValue::Float(transform.y_axis.w),
                    JValue::Float(transform.z_axis.x),
                    JValue::Float(transform.z_axis.y),
                    JValue::Float(transform.z_axis.z),
                    JValue::Float(transform.z_axis.w),
                    JValue::Float(transform.w_axis.x),
                    JValue::Float(transform.w_axis.y),
                    JValue::Float(transform.w_axis.z),
                    JValue::Float(transform.w_axis.w),
                ],
            )
            .unwrap()
            .into_inner()
        })
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_setTransform(
    env: JNIEnv,
    this: jobject,
    transform: jobject,
) {
    catch_panic!(env, {
        let matrix4f_class = env.find_class(MATRIX4F_CLASS).unwrap();

        let get_field = move |name: &str| {
            let field = env.get_field_id(matrix4f_class, name, "F").unwrap();
            env.get_field_unchecked(transform, field, JavaType::Primitive(Primitive::Float))
                .unwrap()
                .f()
                .unwrap()
        };

        let rust_transform = Mat4::from_cols_array(&[
            get_field("m00"),
            get_field("m01"),
            get_field("m02"),
            get_field("m03"),
            get_field("m10"),
            get_field("m11"),
            get_field("m12"),
            get_field("m13"),
            get_field("m20"),
            get_field("m21"),
            get_field("m22"),
            get_field("m23"),
            get_field("m30"),
            get_field("m31"),
            get_field("m32"),
            get_field("m33"),
        ]);

        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.meshes.get_mut(&pointer).expect(MESH_NOT_FOUND_MSG).transform = rust_transform;
        });
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_getTextureOffsets(env: JNIEnv, this: jobject) -> jobject {
    catch_panic!(env, {
        let vector2f_class = env.find_class(VECTOR2F_CLASS).unwrap();
        let vector2f_ctor = (vector2f_class, "(FF)V").lookup(&env).unwrap();
        let new_vector2f = move |vector: Vec2| {
            JValue::Object(
                env.new_object_unchecked(
                    vector2f_class,
                    vector2f_ctor,
                    &[JValue::Float(vector.x), JValue::Float(vector.y)]
                )
                .unwrap()
            )
        };

        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            let texture_offsets = renderer.meshes.get(&pointer).expect(MESH_NOT_FOUND_MSG).texture_offsets;
            let args = [new_vector2f(texture_offsets.0), new_vector2f(texture_offsets.1)];
            env.new_object(PAIR_CLASS, "(Ljava/lang/Object;Ljava/lang/Object;)V", &args).unwrap().into_inner()
        })
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_setTextureOffsets(
    env: JNIEnv,
    this: jobject,
    texture_offsets: jobject,
) {
    catch_panic!(env, {
        let pair_class = env.find_class(PAIR_CLASS).unwrap();
        let pair_first_field = env.get_field_id(pair_class, "first", OBJECT_TYPE).unwrap();
        let pair_second_field = env.get_field_id(pair_class, "second", OBJECT_TYPE).unwrap();

        let vector2f_class = env.find_class(VECTOR2F_CLASS).unwrap();
        let vector2f_x = env.get_field_id(vector2f_class, "x", "F").unwrap();
        let vector2f_y = env.get_field_id(vector2f_class, "y", "F").unwrap();
        let get_vector2f = move |obj: JObject| {
            if !obj.is_null() {
                Vec2::new(
                    env.get_field_unchecked(obj, vector2f_x, JavaType::Primitive(Primitive::Float))
                        .unwrap()
                        .f()
                        .unwrap(),
                    env.get_field_unchecked(obj, vector2f_y, JavaType::Primitive(Primitive::Float))
                        .unwrap()
                        .f()
                        .unwrap(),
                )
            } else {
                Vec2::ZERO
            }
        };

        let offset0 = get_vector2f(
            env.get_field_unchecked(
                texture_offsets,
                pair_first_field,
                JavaType::Object(OBJECT_TYPE.to_string()),
            )
            .unwrap()
            .l()
            .unwrap(),
        );
        let offset1 = get_vector2f(
            env.get_field_unchecked(
                texture_offsets,
                pair_second_field,
                JavaType::Object(OBJECT_TYPE.to_string()),
            )
            .unwrap()
            .l()
            .unwrap(),
        );

        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.meshes.get_mut(&pointer).unwrap().texture_offsets = (offset0, offset1);
        });
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_getOverlayColor(env: JNIEnv, this: jobject) -> jobject {
    catch_panic!(env, {
        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            match renderer.meshes.get(&pointer).expect(MESH_NOT_FOUND_MSG).overlay_color {
                Some(overlay_color) => env.new_object(
                    VECTOR4F_CLASS,
                    "(FFFF)V",
                    &[
                        JValue::Float(overlay_color.x),
                        JValue::Float(overlay_color.y),
                        JValue::Float(overlay_color.z),
                        JValue::Float(overlay_color.w),
                    ],
                )
                .unwrap()
                .into_inner(),
                None => JObject::null().into_inner(),
            }
        })
    } else {
        JObject::null().into_inner()
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_setOverlayColor(
    env: JNIEnv,
    this: jobject,
    overlay_color: jobject,
) {
    catch_panic!(env, {
        let overlay_color_rust = if !overlay_color.is_null() {
            let vector4f_class = env.find_class(VECTOR4F_CLASS).unwrap();

            let vector4f_x = env.get_field_id(vector4f_class, "x", "F").unwrap();
            let vector4f_y = env.get_field_id(vector4f_class, "y", "F").unwrap();
            let vector4f_z = env.get_field_id(vector4f_class, "z", "F").unwrap();
            let vector4f_w = env.get_field_id(vector4f_class, "w", "F").unwrap();

            let x = env
                .get_field_unchecked(overlay_color, vector4f_x, JavaType::Primitive(Primitive::Float))
                .unwrap()
                .f()
                .unwrap();
            let y = env
                .get_field_unchecked(overlay_color, vector4f_y, JavaType::Primitive(Primitive::Float))
                .unwrap()
                .f()
                .unwrap();
            let z = env
                .get_field_unchecked(overlay_color, vector4f_z, JavaType::Primitive(Primitive::Float))
                .unwrap()
                .f()
                .unwrap();
            let w = env
                .get_field_unchecked(overlay_color, vector4f_w, JavaType::Primitive(Primitive::Float))
                .unwrap()
                .f()
                .unwrap();

            Some(Vec4::new(x, y, z, w))
        } else {
            None
        };

        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer
                .meshes
                .get_mut(&pointer)
                .expect(MESH_NOT_FOUND_MSG)
                .overlay_color = overlay_color_rust;
        });
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_getOpacity(env: JNIEnv, this: jobject) -> jfloat {
    catch_panic!(env, {
        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.meshes.get(&pointer).expect(MESH_NOT_FOUND_MSG).opacity
        })
    } else {
        0.0
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastMesh_setOpacity(env: JNIEnv, this: jobject, opacity: jfloat) {
    catch_panic!(env, {
        let pointer = get_mesh_pointer(env, this);

        backend::with_renderer(move |renderer| {
            renderer.meshes.get_mut(&pointer).expect(MESH_NOT_FOUND_MSG).opacity = opacity;
        });
    });
}
