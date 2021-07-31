use glam::{Mat4, Vec4};

/// A `#[repr(C)]` Vec4.
#[repr(C)]
#[derive(Copy, Clone)]
pub struct ForeignVec4 {
    pub x: f32,
    pub y: f32,
    pub z: f32,
    pub w: f32,
}

impl From<Vec4> for ForeignVec4 {
    #[inline]
    fn from(v: Vec4) -> Self {
        Self {
            x: v.x,
            y: v.y,
            z: v.z,
            w: v.w,
        }
    }
}

impl From<ForeignVec4> for Vec4 {
    #[inline]
    fn from(v: ForeignVec4) -> Self {
        Self::new(v.x, v.y, v.z, v.w)
    }
}

/// A `#[repr(C)]` Mat4.
#[repr(C)]
#[derive(Copy, Clone)]
pub struct ForeignMat4 {
    pub x_axis: ForeignVec4,
    pub y_axis: ForeignVec4,
    pub z_axis: ForeignVec4,
    pub w_axis: ForeignVec4,
}

impl From<Mat4> for ForeignMat4 {
    #[inline]
    fn from(v: Mat4) -> Self {
        Self {
            x_axis: v.x_axis.into(),
            y_axis: v.y_axis.into(),
            z_axis: v.z_axis.into(),
            w_axis: v.w_axis.into(),
        }
    }
}

impl From<ForeignMat4> for Mat4 {
    #[inline]
    fn from(v: ForeignMat4) -> Self {
        Self::from_cols(v.x_axis.into(), v.y_axis.into(), v.z_axis.into(), v.w_axis.into())
    }
}
