// Shared definitions between the fragment and vertex shader.

layout(push_constant) uniform PushConstants {
    mat4 model;
    vec4 tex_offsets;
    vec4 overlay_color;
    float opacity;
    int vertex_type;
} push;