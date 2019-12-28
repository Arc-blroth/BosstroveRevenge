#version 330 core

in vec4 vertexColor;
in vec2 outTexCoord;
flat in int outUseTexture;

out vec4 fragColor;

uniform sampler2D texture1;

void main() {
    if(outUseTexture == 1) {
		float text = texture(texture1, outTexCoord).w;
		fragColor = vec4(text, text, text, 1);
	} else {
		fragColor = vertexColor;
	}
} 