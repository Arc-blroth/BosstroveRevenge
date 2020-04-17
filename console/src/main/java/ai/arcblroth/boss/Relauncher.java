package ai.arcblroth.boss;

import ai.arcblroth.boss.util.ThreadUtils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Relauncher {

	public static final String USE_RELAUNCHER = "USE_RELAUNCHER";
	public static final String IS_RELAUNCHED = "IS_RELAUNCHED";

	public static <M> void relaunch(Class<M> main, Runnable afterRelaunch) throws Exception {

		//System.setProperty(FORCE_NOWINDOWS, "true");
		//System.setProperty(FORCE_NOSUBSCRIBINGCLASSLOADER, "true");
		//System.setProperty(FORCE_NORENDER, "true");
		
		// [00:00:00][Logger/LEVEL]: Message
		Environment.setLoggingPattern();

		try {
			boolean useRelauncher = System.getProperty(USE_RELAUNCHER) != null;
			if (System.getProperty(IS_RELAUNCHED) == null && (Environment.IS_ACTUALLY_WINDOWS ? !(useRelauncher && System.getProperty(USE_RELAUNCHER).equals("true")) : useRelauncher)) {
				String javaExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
				String classPath = System.getProperty("java.class.path") + File.pathSeparator
						+ Relauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				String switchRelaunched = "-D" + IS_RELAUNCHED + "=true";
				String className = main.getName();

				if (Environment.IS_ACTUALLY_WINDOWS) {
					new ProcessBuilder("C:\\Windows\\System32\\cmd", "/C", "start", "Bosstrove's Revenge",
							// "echo", "off", "&", "mode", (OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2), "&",
							javaExe, switchRelaunched, "-cp", classPath, className)
									.start().waitFor();
					System.exit(0);
				} else {
					new ProcessBuilder(javaExe, switchRelaunched, "-cp", classPath,
							className).inheritIO().start().waitFor();
					System.exit(0);
				}
			}

			System.out.println("Loading...");
			afterRelaunch.run();
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "FATAL ERROR", e);
			ThreadUtils.waitForever();
		}
	}

}