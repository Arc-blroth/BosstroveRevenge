use bevy::math::Vec4;
use bevy::prelude::Color;

/// An intentionally flawed HSV representation that
/// replicates the original behavior from try2's
/// [`Color.kt`](https://github.com/Arc-blroth/BosstroveRevenge/blob/6fa583e5af7bf4676c04feabc6dec8fa5d0e05fc/core/src/main/kotlin/ai/arcblroth/boss/util/Color.kt).
#[derive(Copy, Clone, PartialEq, Debug)]
pub struct HSVA {
    pub hue: f32,
    pub saturation: f32,
    pub value: f32,
    pub alpha: f32,
}

impl HSVA {
    /// Constructs a new [`HSVA`] with an alpha of `1.0`.
    pub const fn hsv(h: f32, s: f32, v: f32) -> Self {
        Self {
            hue: h,
            saturation: s,
            value: v,
            alpha: 1.0,
        }
    }

    /// Constructs a new [`HSVA`].
    #[allow(clippy::self_named_constructors)]
    pub const fn hsva(h: f32, s: f32, v: f32, a: f32) -> Self {
        Self {
            hue: h,
            saturation: s,
            value: v,
            alpha: a,
        }
    }

    /// Converts a [`HSVA`] to a [`Color`].
    pub fn as_rgba(self) -> Color {
        let Self {
            hue: h,
            saturation: s,
            value: v,
            alpha: a,
        } = self;
        let h = (h * 6.0).rem_euclid(6.0);
        let h_fract = h.fract();

        let x = v * (1.0 - s);
        let y = v * (1.0 - h_fract * s);
        let z = v * (1.0 - (1.0 - h_fract) * s);

        match h {
            _ if (0.0..1.0).contains(&h) => Color::rgba(v, z, x, a),
            _ if (1.0..2.0).contains(&h) => Color::rgba(y, v, x, a),
            _ if (2.0..3.0).contains(&h) => Color::rgba(x, v, z, a),
            _ if (3.0..4.0).contains(&h) => Color::rgba(x, y, v, a),
            _ if (4.0..5.0).contains(&h) => Color::rgba(z, x, v, a),
            _ if (5.0..6.0).contains(&h) => Color::rgba(v, x, y, a),
            _ => Color::rgba(v, v, v, a),
        }
    }

    /// Converts a [`Color`] to a [`HSVA`].
    pub fn from_rgba(rgba: Color) -> Self {
        match rgba {
            Color::Rgba {
                red,
                green,
                blue,
                alpha,
            } => {
                let max_rgb = [red, green, blue].into_iter().reduce(f32::max).unwrap();
                let min_rgb = [red, green, blue].into_iter().reduce(f32::min).unwrap();
                let delta = max_rgb - min_rgb;

                let h = if delta == 0.0 {
                    0.0
                } else {
                    match max_rgb {
                        _ if red == max_rgb => (green - blue) / delta,
                        _ if green == max_rgb => (blue - red) / delta + 2.0,
                        _ if blue == max_rgb => (red - green) / delta + 4.0,
                        _ => panic!("The colors are off the charts!"),
                    }
                };
                let h_positive = h.rem_euclid(6.0) / 6.0;
                let s = if max_rgb == 0.0 { 0.0 } else { delta / max_rgb };

                HSVA::hsva(h_positive, s, max_rgb, alpha)
            }
            _ => HSVA::from_rgba(rgba.as_rgba()),
        }
    }
}

impl From<Vec4> for HSVA {
    fn from(vec: Vec4) -> Self {
        HSVA::hsva(vec.x, vec.y, vec.z, vec.w)
    }
}

impl From<HSVA> for Vec4 {
    fn from(hsva: HSVA) -> Self {
        Vec4::new(hsva.hue, hsva.saturation, hsva.value, hsva.alpha)
    }
}
