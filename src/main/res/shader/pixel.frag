#version 330 core

in vec4 vertexColor;
in vec2 outTexCoord;
flat in int outUseTexture;

out vec4 fragColor;

uniform sampler2D texture1;

void main() {
    if(outUseTexture == 1) {
		fragColor = vec4(vertexColor.xyz, texture(texture1, outTexCoord).w);
	} else {
		fragColor = vertexColor;
	}
} 