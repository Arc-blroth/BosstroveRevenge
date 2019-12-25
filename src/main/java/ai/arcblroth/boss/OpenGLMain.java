package ai.arcblroth.boss;

import ai.arcblroth.boss.io.lwjgl.OpenGLOutputRenderer;

public class OpenGLMain {
	
	public static void main(String[] argz) throws Exception {
		System.setProperty(Relauncher.FORCE_NOWINDOWS, "true");
		Relauncher.relaunch(OpenGLMain.class, () -> {
			BosstrovesRevenge.get().init(new OpenGLOutputRenderer());
			BosstrovesRevenge.get().start();
		});
	}
	
}
