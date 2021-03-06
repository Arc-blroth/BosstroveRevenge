//! Common class! accessor definitions.

use egui::{Color32, TextStyle};
use jni::objects::JObject;

use crate::backend::FullscreenMode;
use crate::jni_types::*;
use crate::renderer::shader::VertexType;
use crate::renderer::texture::TextureSampling;
use crate::{class, enum_class};

class!(PAIR_CLASS, data class JavaPair(
    val first: OBJECT_CLASS,
    val second: OBJECT_CLASS,
));

class!(VECTOR2D_CLASS, class JavaVector2d(
    val x: Double,
    val y: Double,
));

class!(VECTOR2F_CLASS, class JavaVector2f(
    val x: Float,
    val y: Float,
));

class!(VECTOR3D_CLASS, class JavaVector3d(
    val x: Double,
    val y: Double,
    val z: Double,
));

class!(VECTOR3F_CLASS, class JavaVector3f(
    val x: Float,
    val y: Float,
    val z: Float,
));

class!(VECTOR4F_CLASS, class JavaVector4f(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float,
));

class!(MATRIX4F_CLASS, class JavaMatrix4f(
    val m00: Float,
    val m01: Float,
    val m02: Float,
    val m03: Float,
    val m10: Float,
    val m11: Float,
    val m12: Float,
    val m13: Float,
    val m20: Float,
    val m21: Float,
    val m22: Float,
    val m23: Float,
    val m30: Float,
    val m31: Float,
    val m32: Float,
    val m33: Float,
));

class!(COLOR_CLASS, class JavaColor(
    val rgba: Int,
));

impl<'a> JavaColor<'a> {
    /// Converts this JavaColor into an array of [r, g, b, a].
    pub fn as_color32<O: Into<JObject<'a>>>(&self, obj: O) -> Color32 {
        let rgba = self.rgba(obj) as u32;
        Color32::from_rgba_unmultiplied(
            ((rgba >> 16) & 0xFF) as u8,
            ((rgba >> 8) & 0xFF) as u8,
            (rgba & 0xFF) as u8,
            ((rgba >> 24) & 0xFF) as u8,
        )
    }
}

class!(CAMERA_CLASS, class JavaCamera(
    val pos: VECTOR3D_CLASS,
    val yaw: Double,
    val pitch: Double,
    val fov: Double,
));

class!(VERTEX_CLASS, class JavaVertex(
    val pos: VECTOR3F_CLASS,
    val colorTex: VECTOR4F_CLASS,
));

enum_class!(VERTEX_TYPE_CLASS => VertexType, enum class JavaVertexType {
    COLOR => COLOR,
    TEX1 => TEX1,
    TEX2 => TEX2,
});

enum_class!(TEXTURE_SAMPLING_CLASS => TextureSampling, enum class JavaTextureSampling {
    SMOOTH => Smooth,
    PIXEL => Pixel,
});

class!(RENDERER_SETTINGS_CLASS, data class JavaRendererSettings(
    val rendererSize: VECTOR2D_CLASS,
    val fullscreenMode: FULLSCREEN_MODE_CLASS,
    val transparent: Boolean,
));

enum_class!(FULLSCREEN_MODE_CLASS => FullscreenMode, enum class JavaFullscreenMode {
    NONE => None,
    BORDERLESS => Borderless,
    EXCLUSIVE => Exclusive,
});

class!(BOUNDS_CLASS, data class JavaBounds(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
));

enum_class!(TEXT_STYLE_CLASS => TextStyle, enum class JavaTextStyle {
    SMALL => Small,
    BODY => Body,
    BUTTON => Button,
    HEADING => Heading,
    MONOSPACE => Monospace,
});

class!(LABEL_CLASS, data class JavaLabel(
    val text: String,
    val wrap: BOOLEAN_CLASS?,
    val textStyle: TEXT_STYLE_CLASS?,
    val backgroundColor: COLOR_CLASS,
    val textColor: COLOR_CLASS?,
    val code: Boolean,
    val strong: Boolean,
    val weak: Boolean,
    val strikethrough: Boolean,
    val underline: Boolean,
    val italics: Boolean,
    val raised: Boolean,
));

class!(ROAST_TEXTURE_CLASS, class JavaRoastTexture(
    val pointer: Long,
));
