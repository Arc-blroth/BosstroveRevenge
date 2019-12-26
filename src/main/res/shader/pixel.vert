#version 330 core

layout (location = 0) in vec3 pos;

out vec4 vertexColor;

uniform mat4 model;
uniform mat4 projection;
uniform vec3 color;

void main() {
    gl_Position = projection * model * vec4(pos, 1.0);
	vertexColor = vec4(color, 1.0);
}