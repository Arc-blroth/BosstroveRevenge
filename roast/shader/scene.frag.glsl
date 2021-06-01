#version 450
#extension GL_GOOGLE_include_directive: enable

// shared push constant
#include "scene.common.glsl"

layout(binding = 0, set = 1) uniform sampler2D texture1;
layout(binding = 1, set = 1) uniform sampler2D texture2;

layout(location = 0) in vec4 base_color;
layout(location = 1) in vec4 tex;

layout(location = 0) out vec4 out_color;

void main() {
    vec4 color = base_color;
    if(int(push.vertex_type) >= 1) {
        color *= sqrt(texture(texture1, tex.xy + push.tex_offsets.xy));
    }
    if(int(push.vertex_type) == 2) {
        color *= sqrt(texture(texture2, tex.zw + push.tex_offsets.zw));
    }

    out_color = vec4(color.xyz, color.w * push.opacity);
}
