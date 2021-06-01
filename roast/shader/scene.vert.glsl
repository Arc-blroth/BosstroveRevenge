#version 450
#extension GL_GOOGLE_include_directive: enable

// shared push constant
#include "scene.common.glsl"

layout(binding = 0) uniform UniformBufferObject {
    mat4 view;
    mat4 proj;
} ubo;

layout(location = 0) in vec3 pos;
layout(location = 1) in vec4 color_tex;

layout(location = 0) out vec4 base_color;
layout(location = 1) out vec4 tex;

out gl_PerVertex {
    vec4 gl_Position;
};

void main() {
    gl_Position = ubo.proj * ubo.view * push.model * vec4(pos, 1.0);
    if (int(push.vertex_type) == 0) {
        base_color = color_tex * push.overlay_color;
    } else {
        base_color = push.overlay_color;
    }
}
