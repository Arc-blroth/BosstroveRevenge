use std::ops::Deref;
use std::slice;

use glam::{Mat4, Vec3};
use ogt_vox_sys::*;

use crate::renderer::shader::Vertex;
use crate::renderer::types::Index;

/// Wrapper around a `*const ogt_vox_scene`.
struct OgtVoxScene {
    inner: *const ogt_vox_scene,
}

impl OgtVoxScene {
    /// Creates a new VoxScene from the bytes
    /// of a MagicaVoxel file.
    pub fn new(vox: &[u8]) -> Self {
        Self {
            inner: unsafe { ogt_vox_read_scene(vox.as_ptr(), vox.len() as u32) },
        }
    }
}

impl Deref for OgtVoxScene {
    type Target = ogt_vox_scene;

    fn deref(&self) -> &Self::Target {
        assert_ne!(self.inner, std::ptr::null());
        unsafe { &*self.inner }
    }
}

impl Drop for OgtVoxScene {
    fn drop(&mut self) {
        unsafe {
            ogt_vox_destroy_scene(self.inner);
        }
    }
}

/// Wrapper around a `*mut ogt_mesh`.
struct OgtMesh {
    context: ogt_voxel_meshify_context,
    inner: *mut ogt_mesh,
}

impl OgtMesh {
    pub fn from_paletted_voxels_polygon(
        context: ogt_voxel_meshify_context,
        model: &ogt_vox_model,
        palette: &ogt_vox_palette,
    ) -> Self {
        Self {
            context,
            inner: unsafe {
                let mesh = ogt_mesh_from_paletted_voxels_polygon(
                    &context as *const _,
                    model.voxel_data,
                    model.size_x,
                    model.size_y,
                    model.size_z,
                    palette.color.as_ptr() as *const _,
                );
                ogt_mesh_remove_duplicate_vertices(&context as *const _, mesh);
                mesh
            },
        }
    }
}

impl Deref for OgtMesh {
    type Target = ogt_mesh;

    fn deref(&self) -> &Self::Target {
        assert_ne!(self.inner, std::ptr::null_mut());
        unsafe { &*self.inner }
    }
}

impl Drop for OgtMesh {
    fn drop(&mut self) {
        unsafe {
            ogt_mesh_destroy(&self.context as *const _, self.inner);
        }
    }
}

/// Converts an ogt_vox_transform to a `Mat4`.
fn ogt_vox_transform_to_mat4(transform: &ogt_vox_transform) -> Mat4 {
    Mat4::from_cols_array(&[
        transform.m00,
        transform.m01,
        transform.m02,
        transform.m03,
        transform.m10,
        transform.m11,
        transform.m12,
        transform.m13,
        transform.m20,
        transform.m21,
        transform.m22,
        transform.m23,
        transform.m30,
        transform.m31,
        transform.m32,
        transform.m33,
    ])
}

/// Converts a byte in the range 0..255
/// to a float in the range 0.0..1.0.
fn u8_to_f32(x: u8) -> f32 {
    x as f32 / 255.0
}

/// Meshifies the given MagicaVoxel file using
/// the `ogt_mesh_from_paletted_voxels_polygon`
/// algorithm.
pub fn meshify_voxel(vox: &[u8]) -> (Vec<Vertex>, Vec<Index>) {
    let scene = OgtVoxScene::new(vox);

    let mut vertices = Vec::new();
    let mut indices = Vec::new();
    let mut last_max_index = 0;

    // We don't really need to specify a custom allocator
    // so nothing in the context is set.
    let context = ogt_voxel_meshify_context {
        alloc_func: None,
        free_func: None,
        alloc_free_user_data: std::ptr::null_mut(),
    };

    let instances = unsafe { slice::from_raw_parts(scene.instances, scene.num_instances as usize) };
    let models = unsafe { slice::from_raw_parts(scene.models, scene.num_models as usize) };

    for instance in instances {
        // Ignore hidden instances
        if instance.hidden {
            continue;
        }

        let model = unsafe { &*models[instance.model_index as usize] };
        let transform = ogt_vox_transform_to_mat4(&instance.transform);

        let mesh = OgtMesh::from_paletted_voxels_polygon(context, model, &scene.palette);
        let mesh_vertices = unsafe { slice::from_raw_parts(mesh.vertices, mesh.vertex_count as usize) };
        let mesh_indices = unsafe { slice::from_raw_parts(mesh.indices, mesh.index_count as usize) };

        for vertex in mesh_vertices {
            let pos = Vec3::new(vertex.pos.x, vertex.pos.y, vertex.pos.z);
            let transformed_pos = transform * pos.extend(1.0);

            vertices.push(Vertex::pos_color(
                // We use a Y-up system so the position is swizzled here
                [transformed_pos.x, transformed_pos.z, transformed_pos.y],
                [vertex.color.r, vertex.color.g, vertex.color.b, vertex.color.a].map(u8_to_f32),
            ));
        }
        for index in mesh_indices {
            indices.push(*index + last_max_index);
        }

        last_max_index = vertices.len() as u32;
    }

    (vertices, indices)
}
