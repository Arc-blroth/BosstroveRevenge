package ai.arcblroth.boss;

import ai.arcblroth.boss.io.console.AnsiOutputRenderer;

public class OpenGLMain {
	
	public static void main(String[] argz) throws Exception {
		Relauncher.relaunch(ConsoleMain.class, () -> {
			BosstrovesRevenge.get().init(new OpenGLOutputRenderer());
			BosstrovesRevenge.get().start();
		});
	}
	
}
