package ai.arcblroth.boss.io.lwjgl;

import org.joml.Vector3f;

import ai.arcblroth.boss.render.Color;

public class OpenGLUtils {
	
	public static Vector3f rgbToVector(Color in) {
		return new Vector3f(in.getRed()/255F, in.getGreen()/255F, in.getBlue()/255F);
	}
	
}
