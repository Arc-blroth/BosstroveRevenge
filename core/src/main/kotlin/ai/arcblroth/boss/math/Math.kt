package ai.arcblroth.boss.math

/**
 * A vector with 2 float components.
 */
class Vector2f(val x: Float, val y: Float)

/**
 * A vector with 3 float components.
 */
class Vector3f(val x: Float, val y: Float, val z: Float)

/**
 * A vector with 4 float components.
 */
class Vector4f(val x: Float, val y: Float, val z: Float, val w: Float)

/**
 * A 4x4 float matrix.
 */
class Matrix4f(val x_axis: Vector4f, val y_axis: Vector4f, val z_axis: Vector4f, val w_axis: Vector4f)
