package ai.arcblroth.boss;

import ai.arcblroth.boss.io.lwjgl.OpenGLOutputRenderer;

public class OpenGLMain {
	
	public static void main(String[] argz) {
		Environment.setLoggingPattern();
		BosstrovesRevenge.instance().init(new OpenGLOutputRenderer());
		BosstrovesRevenge.instance().start();
	}
	
}
