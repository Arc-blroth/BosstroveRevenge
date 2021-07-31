use glam::{const_vec3, DVec3, Mat4, Vec3};

use crate::renderer::shader::CameraBufferObjectData;

pub const UP: Vec3 = const_vec3!([0.0, 1.0, 0.0]);

#[repr(C)]
pub struct Camera {
    pub pos: DVec3,
    pub yaw: f64,
    pub pitch: f64,
    pub fov: f64,
}

impl Camera {
    pub fn new(pos: DVec3, yaw: f64, pitch: f64, fov: f64) -> Self {
        Self { pos, yaw, pitch, fov }
    }

    pub fn view(&self) -> Mat4 {
        let pos = self.pos.as_f32();
        let front = -Vec3::new(
            (self.yaw.cos() * self.pitch.cos()) as f32,
            self.pitch.sin() as f32,
            (self.yaw.sin() * self.pitch.cos()) as f32,
        )
        .normalize();

        Mat4::look_at_rh(pos, pos + front, UP)
    }

    pub fn view_ortho(&self) -> Mat4 {
        Mat4::look_at_lh(Vec3::new(0.0, 0.0, 0.0), Vec3::new(0.0, 0.0, 1.0), UP)
    }

    pub fn proj(&self, dimensions: [f32; 2]) -> Mat4 {
        let mut proj = Mat4::perspective_lh(self.fov as f32, dimensions[0] / dimensions[1], 0.01, 1000.0);
        proj.y_axis.y *= -1.0;
        proj
    }

    pub fn proj_ortho(dimensions: [f32; 2]) -> Mat4 {
        Mat4::orthographic_rh(0.0, dimensions[0], 0.0, dimensions[1], 0.0, 1.0)
    }

    pub(super) fn update_uniform_buffer(
        &self,
        viewport_dimensions: [f32; 2],
        orthogonal: bool,
    ) -> CameraBufferObjectData {
        if orthogonal {
            CameraBufferObjectData {
                view: self.view_ortho(),
                proj: Self::proj_ortho(viewport_dimensions),
            }
        } else {
            CameraBufferObjectData {
                view: self.view(),
                proj: self.proj(viewport_dimensions),
            }
        }
    }
}

impl Default for Camera {
    fn default() -> Self {
        Camera::new(DVec3::ZERO, 0.0, 0.0, 45.0)
    }
}
