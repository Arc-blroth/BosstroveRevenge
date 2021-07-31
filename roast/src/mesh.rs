//! JNI implementation of the `ai.arcblroth.roast.RoastMesh` native methods.

use glam::Vec2;

use crate::backend;
use crate::error::{catch_panic, ForeignRoastResult, Nothing};
use crate::ffi::math::{ForeignMat4, ForeignVec4};
use crate::ffi::util::{ForeignOption, ForeignPair};
use crate::renderer::shader::VertexType;
use crate::renderer::{MeshId, TextureId};

pub const MESH_NOT_FOUND_MSG: &str = "Mesh pointer does not point to a valid struct";

#[no_mangle]
pub extern "C" fn roast_mesh_get_vertex_type(this: MeshId) -> ForeignRoastResult<VertexType> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| Ok(renderer.meshes.get(&this).expect(MESH_NOT_FOUND_MSG).vertex_type()))
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_get_textures(
    this: MeshId,
) -> ForeignRoastResult<ForeignPair<ForeignOption<TextureId>, ForeignOption<TextureId>>> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            let textures = renderer.meshes.get(&this).expect(MESH_NOT_FOUND_MSG).textures;
            Ok(ForeignPair(textures.0.into(), textures.1.into()))
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_set_textures(
    this: MeshId,
    textures: ForeignPair<ForeignOption<TextureId>, ForeignOption<TextureId>>,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            let textures = (textures.0.into(), textures.1.into());
            renderer.meshes.get_mut(&this).expect(MESH_NOT_FOUND_MSG).textures = textures;
            Ok(Nothing::default())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_get_transform(this: MeshId) -> ForeignRoastResult<ForeignMat4> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            Ok(renderer.meshes.get(&this).expect(MESH_NOT_FOUND_MSG).transform.into())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_set_transform(this: MeshId, transform: ForeignMat4) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            renderer.meshes.get_mut(&this).expect(MESH_NOT_FOUND_MSG).transform = transform.into();
            Ok(Nothing::default())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_get_texture_offsets(this: MeshId) -> ForeignRoastResult<ForeignPair<Vec2, Vec2>> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            Ok(renderer
                .meshes
                .get(&this)
                .expect(MESH_NOT_FOUND_MSG)
                .texture_offsets
                .into())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_set_texture_offsets(
    this: MeshId,
    offsets: ForeignPair<Vec2, Vec2>,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            renderer
                .meshes
                .get_mut(&this)
                .expect(MESH_NOT_FOUND_MSG)
                .texture_offsets = offsets.into();
            Ok(Nothing::default())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_get_overlay_color(this: MeshId) -> ForeignRoastResult<ForeignOption<ForeignVec4>> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            Ok(renderer
                .meshes
                .get(&this)
                .expect(MESH_NOT_FOUND_MSG)
                .overlay_color
                .map(|v| v.into())
                .into())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_set_overlay_color(
    this: MeshId,
    overlay_color: ForeignOption<ForeignVec4>,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            let overlay_color: Option<ForeignVec4> = overlay_color.into();
            renderer.meshes.get_mut(&this).expect(MESH_NOT_FOUND_MSG).overlay_color = overlay_color.map(|v| v.into());
            Ok(Nothing::default())
        })
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_get_opacity(this: MeshId) -> ForeignRoastResult<f32> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| Ok(renderer.meshes.get(&this).expect(MESH_NOT_FOUND_MSG).opacity))
    })
}

#[no_mangle]
pub extern "C" fn roast_mesh_set_opacity(this: MeshId, opacity: f32) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        backend::with_renderer(move |renderer| {
            renderer.meshes.get_mut(&this).expect(MESH_NOT_FOUND_MSG).opacity = opacity;
            Ok(Nothing::default())
        })
    })
}
