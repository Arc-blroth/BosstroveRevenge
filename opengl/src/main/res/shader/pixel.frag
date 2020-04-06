#version 330 core

in vec4 vertexColor;
in vec2 outTexCoord;
flat in int outUseTexture;
flat in int outInvertTexture;

out vec4 fragColor;

uniform sampler2D texture1;

void main() {
    if(outUseTexture == 1) {
		if(outInvertTexture == 0) {
			fragColor = vec4(vertexColor.xyz, texture(texture1, outTexCoord).w);
		} else {
			fragColor = texture(texture1, outTexCoord).wwww;
		}
	} else {
		fragColor = vertexColor;
	}
} 