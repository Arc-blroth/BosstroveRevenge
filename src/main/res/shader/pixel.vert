#version 330 core

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 inTexCoord;

out vec4 vertexColor;
out vec2 outTexCoord;
flat out int outUseTexture;

uniform bool useTexture;
uniform mat4 model;
uniform mat4 projection;
uniform vec4 color;

void main() {
    gl_Position = projection * model * vec4(pos, 1.0);
	vertexColor = color;
	if(useTexture) {
		outUseTexture = 1;
		outTexCoord = inTexCoord;
	} else {
		outUseTexture = 0;	
		outTexCoord = vec2(0.0, 0.0);
	}
}