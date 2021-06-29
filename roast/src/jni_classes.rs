//! Common class! accessor definitions.

use crate::class;
use crate::jni_types::*;

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

class!(RENDERER_SETTINGS_CLASS, data class JavaRendererSettings(
    val rendererSize: VECTOR2D_CLASS,
    val fullscreenMode: FULLSCREEN_MODE_CLASS,
    val transparent: Boolean,
));

class!(VERTEX_CLASS, class JavaVertex(
    val pos: VECTOR3F_CLASS,
    val colorTex: VECTOR4F_CLASS,
));

class!(ROAST_TEXTURE_CLASS, class JavaRoastTexture(
    val pointer: Long,
));
