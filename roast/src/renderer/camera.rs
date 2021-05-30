use glam::{const_vec3, Mat4, Vec3};

use crate::renderer::shader::CameraBufferObjectData;

pub const UP: Vec3 = const_vec3!([0.0, 1.0, 0.0]);

pub struct Camera {
    pub pos: Vec3,
    pub yaw: f64,
    pub pitch: f64,
    pub fov: f32,
}

impl Camera {
    pub fn new(pos: Vec3, yaw: f64, pitch: f64, fov: f32) -> Self {
        Self { pos, yaw, pitch, fov }
    }

    pub fn view(&self) -> Mat4 {
        let front = -Vec3::new(
            (self.yaw.cos() * self.pitch.cos()) as f32,
            self.pitch.sin() as f32,
            (self.yaw.sin() * self.pitch.cos()) as f32,
        )
        .normalize();

        Mat4::look_at_rh(self.pos, self.pos + front, UP)
    }

    pub fn proj(&self, dimensions: [f32; 2]) -> Mat4 {
        let mut proj = Mat4::perspective_rh_gl(
            self.fov.clone(),
            dimensions[0] as f32 / dimensions[1] as f32,
            0.01,
            1000.0,
        );
        proj.y_axis.y *= -1.0;
        proj
    }

    pub(super) fn update_uniform_buffer(&self, swap_chain_dimensions: [u32; 2]) -> CameraBufferObjectData {
        CameraBufferObjectData {
            view: self.view(),
            proj: self.proj([swap_chain_dimensions[0] as f32, swap_chain_dimensions[1] as f32]),
        }
    }
}

impl Default for Camera {
    fn default() -> Self {
        Camera::new(Vec3::ZERO, 0.0, 0.0, 45.0)
    }
}
