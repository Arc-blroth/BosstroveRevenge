package ai.arcblroth.boss;

import ai.arcblroth.boss.io.lwjgl.OpenGLOutputRenderer;

public class OpenGLMain {
	
	public static void main(String[] argz) throws Exception {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT][%3$s/%4$s]: %5$s %6$s%n");
		BosstrovesRevenge.instance().init(new OpenGLOutputRenderer());
		BosstrovesRevenge.instance().start();
	}
	
}
